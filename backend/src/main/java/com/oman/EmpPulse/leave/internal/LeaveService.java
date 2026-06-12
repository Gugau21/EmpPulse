package com.oman.EmpPulse.leave.internal;

import com.oman.EmpPulse.leave.dto.LeaveCreateRequest;
import com.oman.EmpPulse.leave.dto.LeaveListResponse;
import com.oman.EmpPulse.leave.dto.LeaveResponse;
import com.oman.EmpPulse.leave.dto.LeaveResponseRequest;
import com.oman.EmpPulse.leave.dto.LeaveUpdateRequest;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LeaveService {

  private final LeaveRepository leaveRepository;
  private final EmployeeApi employeeApi;
  private final AdminApi adminApi;

  public LeaveService(LeaveRepository leaveRepository, EmployeeApi employeeApi, AdminApi adminApi) {
    this.leaveRepository = leaveRepository;
    this.employeeApi = employeeApi;
    this.adminApi = adminApi;
  }

  /**
   * Creates a leave request, either for the caller themselves or on behalf of another employee.
   *
   * <p>The target is the employee identified by {@code req.employeeId} (required); the on-behalf
   * path is taken when that id differs from {@code callerId}. Creating on behalf is allowed only
   * for an admin who oversees the target's department (the owner oversees every department).
   *
   * <p>A request is auto-approved when filed by an admin who oversees the employee's department
   * (including on-behalf creation and an admin's own request in a department they manage), and that
   * admin is recorded as the reviewer; an employee's self-request stays {@code pending}.
   *
   * @param req the request payload; employeeId selects the target employee
   * @param callerId the user ID of the authenticated caller
   * @param isAdmin whether the caller has the ADMIN authority; only gates {@code adminComment}
   * @return the created leave request
   */
  @Transactional
  public LeaveResponse createLeaveRequest(LeaveCreateRequest req, Long callerId, boolean isAdmin) {
    ensureRequiredFieldsPresent(req);
    EmployeeSummaryResponse employee =
        employeeApi
            .findSummaryById(req.getEmployeeId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

    requireActive(employee);
    validateAdminComment(req.getAdminComment(), isAdmin);
    validateDateRange(req.getStartDate(), req.getEndDate());
    validatePersonalLeaveReason(req.getType(), req.getReason());

    if (leaveRepository.existsActiveOverlap(
        employee.getId(), req.getStartDate(), req.getEndDate())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Overlapping leave request exists");
    }

    boolean onBehalf = !req.getEmployeeId().equals(callerId);
    boolean adminOversees = adminApi.overseesDepartment(callerId, employee.getDepartmentId());

    if (onBehalf && !adminOversees) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this employee");
    }

    // adminOversees here means an Admin who manages himself
    LeaveStatus status = adminOversees ? LeaveStatus.approved : LeaveStatus.pending;
    Long adminReviewerId = adminOversees ? callerId : null;

    Leave leave =
        leaveRepository.save(
            new Leave(
                employee.getId(),
                req.getType(),
                req.getStartDate(),
                req.getEndDate(),
                req.getPaid(),
                status,
                req.getReason(),
                adminReviewerId,
                req.getAdminComment()));
    return toLeaveResponse(leave, employee);
  }

  /**
   * Lists the leave requests visible to the caller, sorted by last change, newest first.
   *
   * <p>Admins see the requests of employees in departments they oversee plus their own (the owner
   * sees all); everyone else (employees) sees only their own.
   *
   * @param callerId the user ID of the authenticated caller
   * @param isAdmin whether the caller has the ADMIN authority
   * @return the visible leave requests
   */
  @Transactional(readOnly = true)
  public LeaveListResponse listLeaveRequests(Long callerId, boolean isAdmin) {
    List<Long> deptIds = isAdmin ? adminApi.departmentIdsForAdminUser(callerId) : List.of();
    List<Leave> leaves =
        deptIds.isEmpty()
            ? leaveRepository.findAllByEmployeeIdOrderByUpdatedAtDesc(callerId)
            : leaveRepository.findVisibleToAdmin(callerId, deptIds);

    List<Long> employeeIds = leaves.stream().map(Leave::getEmployeeId).distinct().toList();
    Map<Long, EmployeeSummaryResponse> summaries = employeeApi.findSummariesByIds(employeeIds);

    List<LeaveResponse> items =
        leaves.stream()
            .map(
                leave -> {
                  EmployeeSummaryResponse employee = summaries.get(leave.getEmployeeId());
                  if (employee == null) {
                    throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency");
                  }
                  return toLeaveResponse(leave, employee);
                })
            .toList();
    return new LeaveListResponse(items);
  }

  @Transactional(readOnly = true)
  public LeaveResponse getLeaveRequest(Long leaveRequestId, Long callerId) {
    Leave leave = findLeaveOrThrow(leaveRequestId);
    EmployeeSummaryResponse employee = findEmployeeForLeave(leave);

    boolean ownRequest = leave.getEmployeeId().equals(callerId);
    boolean adminOversees = adminApi.overseesDepartment(callerId, employee.getDepartmentId());
    requireLeaveAccess(ownRequest, adminOversees);
    return toLeaveResponse(leave, employee);
  }

  /**
   * Hard deletes a pending leave request.
   *
   * <p>Deletion is allowed only for the employee the request belongs to and only while the request
   * is {@code pending}; admins and the owner cannot delete requests (they reject them instead).
   *
   * @param leaveRequestId the leave request ID to delete
   * @param callerId the user ID of the authenticated caller
   */
  @Transactional
  public void deleteLeaveRequest(Long leaveRequestId, Long callerId) {
    Leave leave = findLeaveOrThrow(leaveRequestId);

    if (!leave.getEmployeeId().equals(callerId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only the employee the request belongs to can delete it");
    }
    if (leave.getStatus() != LeaveStatus.pending) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Only pending leave requests can be deleted");
    }
    leaveRepository.delete(leave);
  }

  /**
   * Applies a partial in-place update to a leave request; fields left null keep their value.
   *
   * <p>Employees can edit only their own {@code pending} requests. Admins who oversee the
   * employee's department can edit {@code pending} and {@code approved} requests; their edit
   * approves a pending request and records them as the reviewer. An employee's own {@code approved}
   * request is not editable in place (a modification request will cover that), and {@code
   * rejected}/{@code cancelled} requests are never editable.
   *
   * @param leaveRequestId the leave request ID to update
   * @param req the partial update payload
   * @param callerId the user ID of the authenticated caller
   * @param isAdmin whether the caller has the ADMIN authority; only gates {@code adminComment}
   * @return the updated leave request
   */
  @Transactional
  public LeaveResponse updateLeaveRequest(
      Long leaveRequestId, LeaveUpdateRequest req, Long callerId, boolean isAdmin) {
    Leave leave = findLeaveOrThrow(leaveRequestId);
    EmployeeSummaryResponse employee = findEmployeeForLeave(leave);

    boolean ownRequest = leave.getEmployeeId().equals(callerId);
    boolean adminOversees = adminApi.overseesDepartment(callerId, employee.getDepartmentId());
    validateUpdatePermissions(
        ownRequest, adminOversees, isAdmin, leave.getStatus(), req.getAdminComment());

    ResolvedFields fields = resolveFields(req, leave);
    validateLeaveUpdate(fields, leave.getEmployeeId(), leave.getId());
    applyLeaveUpdate(leave, fields, req.getAdminComment(), adminOversees, callerId);
    return toLeaveResponse(leaveRepository.saveAndFlush(leave), employee);
  }

  /**
   * Records an admin's decision on a pending leave request, setting its status to {@code approved}
   * or {@code rejected} and stamping the caller as the reviewer.
   *
   * <p>Allowed only for an admin who oversees the employee's department (the owner oversees every
   * department). Only {@code pending} requests can be responded to; any other status is a 409. An
   * optional {@code adminComment} is attached when present.
   *
   * @param leaveRequestId the leave request ID to respond to
   * @param req the decision payload; status must be {@code approved} or {@code rejected}
   * @param callerId the user ID of the authenticated caller
   * @return the updated leave request
   */
  @Transactional
  public LeaveResponse respondToLeaveRequest(
      Long leaveRequestId, LeaveResponseRequest req, Long callerId) {
    Leave leave = findLeaveOrThrow(leaveRequestId);
    EmployeeSummaryResponse employee = findEmployeeForLeave(leave);

    if (!adminApi.overseesDepartment(callerId, employee.getDepartmentId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this leave request");
    }
    if (req.getStatus() != LeaveStatus.approved && req.getStatus() != LeaveStatus.rejected) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Decision must be approved or rejected");
    }
    if (leave.getStatus() != LeaveStatus.pending) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Only pending leave requests can be approved or rejected");
    }

    // TODO(modification requests): once POST /{id}/modifications exists, approving/rejecting a
    // request that carries a modificationId must resolve the linked modification (approve
    // overwrites the original and deletes the mod row; reject unlinks it to a standalone rejected
    // request).
    leave.setStatus(req.getStatus());
    leave.setAdminReviewerId(callerId);
    if (req.getAdminComment() != null) {
      leave.setAdminComment(req.getAdminComment());
    }
    return toLeaveResponse(leaveRepository.saveAndFlush(leave), employee);
  }

  /**
   * Cancels the caller's own approved leave request, setting its status to {@code cancelled}.
   *
   * <p>Allowed only for the employee the request belongs to and only while the request is {@code
   * approved}.
   *
   * @param leaveRequestId the leave request ID to cancel
   * @param callerId the user ID of the authenticated caller
   * @return the cancelled leave request
   */
  @Transactional
  public LeaveResponse cancelLeaveRequest(Long leaveRequestId, Long callerId) {
    Leave leave = findLeaveOrThrow(leaveRequestId);

    if (!leave.getEmployeeId().equals(callerId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only the employee the request belongs to can cancel it");
    }
    if (leave.getStatus() != LeaveStatus.approved) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Only approved leave requests can be cancelled");
    }

    // TODO(modification requests): once POST /{id}/modifications exists, cancelling a request that
    // has a pending modification linked via modificationId should set the modificationId of that
    // pending
    // modifcation to null. In short: in this case the pending modification becomes like any normal
    // pending request
    leave.setStatus(LeaveStatus.cancelled);
    EmployeeSummaryResponse employee = findEmployeeForLeave(leave);
    return toLeaveResponse(leaveRepository.saveAndFlush(leave), employee);
  }

  private void validateUpdatePermissions(
      boolean ownRequest,
      boolean adminOversees,
      boolean isAdmin,
      LeaveStatus status,
      String adminComment) {
    requireLeaveAccess(ownRequest, adminOversees);
    if (status == LeaveStatus.rejected || status == LeaveStatus.cancelled) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Rejected or cancelled requests cannot be edited");
    }
    validateAdminComment(adminComment, isAdmin);
    // TODO(modification requests): once POST /{id}/modifications exists, reject edits with 409
    // when a pending modification request references this leave (existsByModificationId).
    if (!adminOversees && status == LeaveStatus.approved) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Approved requests are edited via a modification request");
    }
  }

  private record ResolvedFields(
      LeaveType type, boolean paid, LocalDate startDate, LocalDate endDate, String reason) {}

  private ResolvedFields resolveFields(LeaveUpdateRequest req, Leave leave) {
    return new ResolvedFields(
        req.getType() != null ? req.getType() : leave.getType(),
        req.getPaid() != null ? req.getPaid() : leave.isPaid(),
        req.getStartDate() != null ? req.getStartDate() : leave.getStartDate(),
        req.getEndDate() != null ? req.getEndDate() : leave.getEndDate(),
        req.getReason() != null ? req.getReason() : leave.getDescription());
  }

  private void validateLeaveUpdate(ResolvedFields fields, Long employeeId, Long excludeLeaveId) {
    validateDateRange(fields.startDate(), fields.endDate());
    validatePersonalLeaveReason(fields.type(), fields.reason());
    if (leaveRepository.existsActiveOverlapExcluding(
        employeeId, fields.startDate(), fields.endDate(), excludeLeaveId)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Overlapping leave request exists");
    }
  }

  private void applyLeaveUpdate(
      Leave leave,
      ResolvedFields fields,
      String adminComment,
      boolean adminOversees,
      Long callerId) {
    leave.setType(fields.type());
    leave.setPaid(fields.paid());
    leave.setStartDate(fields.startDate());
    leave.setEndDate(fields.endDate());
    leave.setDescription(fields.reason());
    if (adminComment != null) {
      leave.setAdminComment(adminComment);
    }
    if (adminOversees) {
      leave.setStatus(LeaveStatus.approved);
      leave.setAdminReviewerId(callerId);
    }
  }

  private EmployeeSummaryResponse findEmployeeForLeave(Leave leave) {
    return employeeApi
        .findSummaryById(leave.getEmployeeId())
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));
  }

  private Leave findLeaveOrThrow(Long leaveRequestId) {
    return leaveRepository
        .findById(leaveRequestId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
  }

  private void ensureRequiredFieldsPresent(LeaveCreateRequest req) {
    if (req.getEmployeeId() == null
        || req.getType() == null
        || req.getPaid() == null
        || req.getStartDate() == null
        || req.getEndDate() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "employeeId, type, paid, startDate and endDate are required");
    }
  }

  private void requireLeaveAccess(boolean ownRequest, boolean adminOversees) {
    if (!ownRequest && !adminOversees) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this leave request");
    }
  }

  private void validateAdminComment(String adminComment, boolean isAdmin) {
    if (adminComment != null && !isAdmin) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "adminComment is allowed only for administrators");
    }
  }

  private void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (endDate.isBefore(startDate)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "End date must not be before start date");
    }
  }

  private void validatePersonalLeaveReason(LeaveType type, String reason) {
    if (type == LeaveType.personal && (reason == null || reason.isBlank())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Reason is required for personal leave");
    }
  }

  private void requireActive(EmployeeSummaryResponse employee) {
    if (!employee.isActive()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee is not active");
    }
  }

  private LeaveResponse toLeaveResponse(Leave leave, EmployeeSummaryResponse employee) {
    return new LeaveResponse(
        leave.getId(),
        employee,
        leave.getType(),
        leave.isPaid(),
        leave.getStartDate(),
        leave.getEndDate(),
        leave.getDescription(),
        leave.getStatus(),
        leave.getAdminReviewerId(),
        leave.getAdminComment(),
        leave.getCreatedAt(),
        leave.getUpdatedAt());
  }
}
