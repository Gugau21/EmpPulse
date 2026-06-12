package com.oman.EmpPulse.leave.internal;

import com.oman.EmpPulse.leave.dto.LeaveCreateRequest;
import com.oman.EmpPulse.leave.dto.LeaveListResponse;
import com.oman.EmpPulse.leave.dto.LeaveResponse;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
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

    if (req.getAdminComment() != null && !isAdmin) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "adminComment is allowed only for administrators");
    }
    if (req.getEndDate().isBefore(req.getStartDate())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "End date must not be before start date");
    }
    if (req.getType() == LeaveType.personal
        && (req.getReason() == null || req.getReason().isBlank())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Reason is required for personal leave");
    }
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
    EmployeeSummaryResponse employee =
        employeeApi
            .findSummaryById(leave.getEmployeeId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));

    boolean own = leave.getEmployeeId().equals(callerId);
    if (!own && !adminApi.overseesDepartment(callerId, employee.getDepartmentId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this leave request");
    }
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
