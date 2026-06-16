package com.oman.EmpPulse.leave.internal;

import com.oman.EmpPulse.leave.api.ActiveLeaveResponse;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.leave.dto.LeaveCreateRequest;
import com.oman.EmpPulse.leave.dto.LeaveListResponse;
import com.oman.EmpPulse.leave.dto.LeaveModificationRequest;
import com.oman.EmpPulse.leave.dto.LeaveResponse;
import com.oman.EmpPulse.leave.dto.LeaveResponseRequest;
import com.oman.EmpPulse.leave.dto.LeaveUpdateRequest;
import com.oman.EmpPulse.loggedhours.api.LoggedHoursApi;
import com.oman.EmpPulse.notification.api.LeaveNotificationDetails;
import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.notification.api.NotificationRecipient;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import com.oman.EmpPulse.user.api.UserApi;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LeaveService implements LeaveApi {

  private final LeaveRepository leaveRepository;
  private final EmployeeApi employeeApi;
  private final AdminApi adminApi;
  private final LoggedHoursApi loggedHoursApi;
  private final UserApi userApi;
  private final NotificationApi notificationApi;

  public LeaveService(
      LeaveRepository leaveRepository,
      EmployeeApi employeeApi,
      AdminApi adminApi,
      LoggedHoursApi loggedHoursApi,
      UserApi userApi,
      NotificationApi notificationApi) {
    this.leaveRepository = leaveRepository;
    this.employeeApi = employeeApi;
    this.adminApi = adminApi;
    this.loggedHoursApi = loggedHoursApi;
    this.userApi = userApi;
    this.notificationApi = notificationApi;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, ActiveLeaveResponse> findActiveLeavesByEmployeeIds(
      Collection<Long> employeeIds) {
    if (employeeIds == null || employeeIds.isEmpty()) {
      return Map.of();
    }
    List<Leave> activeLeaves =
        leaveRepository.findActiveApprovedByEmployeeIds(employeeIds, LocalDate.now());
    Map<Long, ActiveLeaveResponse> result = new HashMap<>();
    activeLeaves.stream()
        .collect(Collectors.groupingBy(Leave::getEmployeeId))
        .forEach(
            (employeeId, leaves) -> {
              if (leaves.size() > 1) {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency");
              }
              result.put(employeeId, toActiveLeaveResponse(leaves.getFirst()));
            });
    return result;
  }

  private ActiveLeaveResponse toActiveLeaveResponse(Leave leave) {
    return new ActiveLeaveResponse(leave.getType(), leave.getStartDate(), leave.getEndDate());
  }

  @Override
  @Transactional(readOnly = true)
  public int countUsedVacationDays(Long employeeId, int year) {
    LocalDate yearStart = LocalDate.of(year, 1, 1);
    LocalDate yearEnd = LocalDate.of(year, 12, 31);
    int total = 0;
    for (Leave leave :
        leaveRepository.findActiveVacationLeavesOverlapping(employeeId, yearStart, yearEnd)) {
      LocalDate from = leave.getStartDate().isBefore(yearStart) ? yearStart : leave.getStartDate();
      LocalDate to = leave.getEndDate().isAfter(yearEnd) ? yearEnd : leave.getEndDate();
      total += countWeekdays(from, to);
    }
    return total;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Long> findEmployeeIdsOnUnpaidApprovedLeave(
      Collection<Long> employeeIds, LocalDate date) {
    if (employeeIds == null || employeeIds.isEmpty()) {
      return Set.of();
    }
    return new HashSet<>(leaveRepository.findEmployeeIdsOnUnpaidApprovedLeave(employeeIds, date));
  }

  private static int countWeekdays(LocalDate from, LocalDate to) {
    int weekdays = 0;
    for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
      DayOfWeek dow = day.getDayOfWeek();
      if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
        weekdays++;
      }
    }
    return weekdays;
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
    clearLoggedHoursIfUnpaidApproved(leave);

    if (onBehalf) {
      userApi
          .findContactById(employee.getId())
          .ifPresent(
              c ->
                  notificationApi.sendLeaveCreatedOnBehalf(
                      new NotificationRecipient(c.email(), c.name()), detailsOf(leave)));
    }

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
    if (leaveRepository.existsByModificationId(leave.getId())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Request has a pending modification; resolve it first");
    }

    ResolvedFields fields = resolveFields(req, leave);
    // A modification and the original are linked, so they are both exempt from the overlap rule
    List<Long> overlapExcludeIds =
        leave.getModificationId() != null
            ? List.of(leave.getId(), leave.getModificationId())
            : List.of(leave.getId());
    validateLeaveUpdate(fields, leave.getEmployeeId(), overlapExcludeIds);
    applyLeaveUpdate(leave, fields, req.getAdminComment(), adminOversees, callerId);
    Leave saved = leaveRepository.saveAndFlush(leave);
    clearLoggedHoursIfUnpaidApproved(saved);

    if (adminOversees && !ownRequest) {
      userApi
          .findContactById(saved.getEmployeeId())
          .ifPresent(
              c ->
                  notificationApi.sendLeaveModifiedByAdmin(
                      new NotificationRecipient(c.email(), c.name()), detailsOf(saved)));
    }

    return toLeaveResponse(saved, employee);
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

    if (leave.getModificationId() != null) {
      return resolveModification(leave, req, callerId, employee);
    }

    leave.setStatus(req.getStatus());
    leave.setAdminReviewerId(callerId);
    if (req.getAdminComment() != null) {
      leave.setAdminComment(req.getAdminComment());
    }
    Leave saved = leaveRepository.saveAndFlush(leave);
    clearLoggedHoursIfUnpaidApproved(saved);
    notifyLeaveDecision(employee.getId(), saved, req.getStatus() == LeaveStatus.approved);
    return toLeaveResponse(saved, employee);
  }

  /**
   * Resolves a pending modification request.
   *
   * <p>Approving overrides the original with the modification's values and self-deletes the
   * modification.
   *
   * <p>Rejecting unlinks the modification, which becomes a standalone rejected request. The
   * original is left approved on rejection.
   */
  private LeaveResponse resolveModification(
      Leave modification,
      LeaveResponseRequest req,
      Long callerId,
      EmployeeSummaryResponse employee) {
    if (req.getStatus() == LeaveStatus.approved) {
      Leave original =
          leaveRepository
              .findById(modification.getModificationId())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));
      original.setType(modification.getType());
      original.setPaid(modification.isPaid());
      original.setStartDate(modification.getStartDate());
      original.setEndDate(modification.getEndDate());
      original.setDescription(modification.getDescription());
      original.setAdminReviewerId(callerId);
      if (req.getAdminComment() != null) {
        original.setAdminComment(req.getAdminComment());
      }
      Leave savedOriginal = leaveRepository.saveAndFlush(original);
      clearLoggedHoursIfUnpaidApproved(savedOriginal);
      notifyLeaveDecision(employee.getId(), savedOriginal, true);
      LeaveResponse response = toLeaveResponse(savedOriginal, employee);
      leaveRepository.delete(modification);
      return response;
    }

    modification.setStatus(LeaveStatus.rejected);
    modification.setAdminReviewerId(callerId);
    if (req.getAdminComment() != null) {
      modification.setAdminComment(req.getAdminComment());
    }
    modification.setModificationId(null);
    Leave savedModification = leaveRepository.saveAndFlush(modification);
    notifyLeaveDecision(employee.getId(), savedModification, false);
    return toLeaveResponse(savedModification, employee);
  }

  /**
   * Deletes the employee's logged hours within the leave's date range when the leave is approved
   * and unpaid. Called wherever a leave reaches its final approved state.
   */
  private void clearLoggedHoursIfUnpaidApproved(Leave leave) {
    if (leave.getStatus() == LeaveStatus.approved && !leave.isPaid()) {
      loggedHoursApi.deleteByEmployeeAndDateRange(
          leave.getEmployeeId(), leave.getStartDate(), leave.getEndDate());
    }
  }

  private LeaveNotificationDetails detailsOf(Leave leave) {
    return new LeaveNotificationDetails(
        leave.getType().name(), leave.getStartDate(), leave.getEndDate(), leave.isPaid());
  }

  /** Emails the employee that their leave request was approved or rejected. */
  private void notifyLeaveDecision(Long employeeId, Leave leave, boolean approved) {
    userApi
        .findContactById(employeeId)
        .ifPresent(
            c ->
                notificationApi.sendLeaveDecision(
                    new NotificationRecipient(c.email(), c.name()), detailsOf(leave), approved));
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

    leave.setStatus(LeaveStatus.cancelled);
    // A pending modification linked to this request is unlinked and acts as normal pending request.
    leaveRepository
        .findByModificationId(leave.getId())
        .ifPresent(
            modification -> {
              modification.setModificationId(null);
              leaveRepository.save(modification);
            });
    EmployeeSummaryResponse employee = findEmployeeForLeave(leave);
    return toLeaveResponse(leaveRepository.saveAndFlush(leave), employee);
  }

  /**
   * Creates a modification request for the caller's own approved leave request: a new {@code
   * pending} leave holding the proposed values and linked to the original.
   *
   * <p>Allowed only for the employee the request belongs to and only while the original is {@code
   * approved}; at most one pending modification may exist per request, and at least one field must
   * change.
   *
   * @param leaveRequestId the original (approved) leave request ID
   * @param req the proposed changes; fields left null inherit the original's value
   * @param callerId the user ID of the authenticated caller
   * @return the created modification request
   */
  @Transactional
  public LeaveResponse createModification(
      Long leaveRequestId, LeaveModificationRequest req, Long callerId) {
    Leave original = findLeaveOrThrow(leaveRequestId);

    if (!original.getEmployeeId().equals(callerId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only the employee the request belongs to can modify it");
    }
    if (original.getStatus() != LeaveStatus.approved) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Modifications can only be proposed for approved requests");
    }
    if (leaveRepository.existsByModificationId(original.getId())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "A pending modification already exists for this request");
    }

    ResolvedFields fields = resolveFields(req, original);
    if (isUnchanged(fields, original)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "A modification must change at least one field");
    }
    validateLeaveUpdate(fields, original.getEmployeeId(), List.of(original.getId()));

    Leave modification =
        new Leave(
            original.getEmployeeId(),
            fields.type(),
            fields.startDate(),
            fields.endDate(),
            fields.paid(),
            LeaveStatus.pending,
            fields.reason(),
            null,
            null);
    modification.setModificationId(original.getId());
    return toLeaveResponse(leaveRepository.save(modification), findEmployeeForLeave(original));
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

  private ResolvedFields resolveFields(LeaveModificationRequest req, Leave leave) {
    return new ResolvedFields(
        req.getType() != null ? req.getType() : leave.getType(),
        req.getPaid() != null ? req.getPaid() : leave.isPaid(),
        req.getStartDate() != null ? req.getStartDate() : leave.getStartDate(),
        req.getEndDate() != null ? req.getEndDate() : leave.getEndDate(),
        req.getReason() != null ? req.getReason() : leave.getDescription());
  }

  private boolean isUnchanged(ResolvedFields fields, Leave leave) {
    return fields.type() == leave.getType()
        && fields.paid() == leave.isPaid()
        && fields.startDate().equals(leave.getStartDate())
        && fields.endDate().equals(leave.getEndDate())
        && Objects.equals(fields.reason(), leave.getDescription());
  }

  private void validateLeaveUpdate(
      ResolvedFields fields, Long employeeId, Collection<Long> excludeLeaveIds) {
    validateDateRange(fields.startDate(), fields.endDate());
    validatePersonalLeaveReason(fields.type(), fields.reason());
    if (leaveRepository.existsActiveOverlapExcludingIds(
        employeeId, fields.startDate(), fields.endDate(), excludeLeaveIds)) {
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
