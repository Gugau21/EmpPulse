package com.oman.EmpPulse.leave.internal;

import com.oman.EmpPulse.leave.dto.LeaveCreateRequest;
import com.oman.EmpPulse.leave.dto.LeaveResponse;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
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
   * for the owner or an admin who oversees the target's department, and such requests are always
   * auto-approved.
   *
   * <p>Self requests are created as {@code pending}, unless the caller is an admin who oversees the
   * department their own employee profile is in; then they are auto-approved.
   *
   * <p>Auto-approved requests store the caller as {@code adminReviewerId} when the caller actually
   * oversees the department. The owner has no admin row, so owner-created requests keep a null
   * reviewer (see the TODO below).
   *
   * @param req the request payload; employeeId selects the target employee
   * @param callerId the user ID of the authenticated caller
   * @param isAdmin whether the caller has the ADMIN authority; only gates {@code adminComment}
   * @param isOwner whether the caller has the OWNER authority
   * @return the created leave request
   */
  @Transactional
  public LeaveResponse createLeaveRequest(
      LeaveCreateRequest req, Long callerId, boolean isAdmin, boolean isOwner) {
    ensureRequiredFieldsPresent(req);
    EmployeeSummaryResponse employee =
        employeeApi
            .findSummaryById(req.getEmployeeId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    requireActive(employee);

    if (req.getAdminComment() != null && !isAdmin && !isOwner) {
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

    if (onBehalf && !isOwner && !adminOversees) { // can remove isOwner after migration
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this employee");
    }

    // TODO(simplify): onBehalf here implies the caller is the owner or an overseeing admin, so it
    // always approves.
    LeaveStatus status = adminOversees || onBehalf ? LeaveStatus.approved : LeaveStatus.pending;
    // TODO: Migration — the owner has no admin row, so the admin_reviewer_id FK forbids storing
    // their id. Once the owner becomes an admin of every department, oversees becomes true for
    // them and this naturally stores callerId.
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
