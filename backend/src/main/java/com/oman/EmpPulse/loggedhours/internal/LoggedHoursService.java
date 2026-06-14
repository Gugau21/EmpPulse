package com.oman.EmpPulse.loggedhours.internal;

import com.oman.EmpPulse.loggedhours.dto.LoggedHoursCreateRequest;
import com.oman.EmpPulse.loggedhours.dto.LoggedHoursListResponse;
import com.oman.EmpPulse.loggedhours.dto.LoggedHoursResponse;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoggedHoursService {

  private final LoggedHoursRepository loggedHoursRepository;
  private final EmployeeApi employeeApi;
  private final AdminApi adminApi;

  public LoggedHoursService(
      LoggedHoursRepository loggedHoursRepository, EmployeeApi employeeApi, AdminApi adminApi) {
    this.loggedHoursRepository = loggedHoursRepository;
    this.employeeApi = employeeApi;
    this.adminApi = adminApi;
  }

  private record MergedInterval(LocalTime start, LocalTime end) {}

  /**
   * Scans all logged-hours rows for {@code employeeId} on {@code date} (excluding {@code excludeId}
   * if non-null), finds every row that overlaps or is adjacent to {@code [newStart, newEnd]},
   * deletes those rows, and returns the widest interval that covers the input and all matched rows.
   *
   * <p>Callers must save the resulting interval themselves (as a new row for POST, or by updating
   * the existing row for PATCH). Passing {@code excludeId} for PATCH prevents the row being updated
   * from merging with itself.
   */
  MergedInterval mergeIntervalsForDay(
      Long employeeId, LocalDate date, LocalTime newStart, LocalTime newEnd, Long excludeId) {
    List<LoggedHours> sameDay = loggedHoursRepository.findAllByEmployeeIdAndDate(employeeId, date);
    List<LoggedHours> toMerge =
        sameDay.stream()
            .filter(e -> excludeId == null || !e.getId().equals(excludeId))
            .filter(e -> !e.getStartTime().isAfter(newEnd) && !e.getEndTime().isBefore(newStart))
            .toList();

    LocalTime mergedStart = newStart;
    LocalTime mergedEnd = newEnd;
    for (LoggedHours existing : toMerge) {
      if (existing.getStartTime().isBefore(mergedStart)) mergedStart = existing.getStartTime();
      if (existing.getEndTime().isAfter(mergedEnd)) mergedEnd = existing.getEndTime();
    }
    loggedHoursRepository.deleteAll(toMerge);
    return new MergedInterval(mergedStart, mergedEnd);
  }

  /**
   * Logs a single-day working interval for an employee on behalf of an admin.
   *
   * <p>If the new interval overlaps or is adjacent to existing logs for the same employee on the
   * same date, they are merged into a single log using the earliest start and latest end time.
   *
   * @param employeeId the employee the hours are logged for
   * @param req the interval payload (date, startTime, endTime)
   * @param callerAdminId the user ID of the authenticated admin
   * @return the created (or merged) logged hours entry
   */
  @Transactional
  public LoggedHoursResponse createLoggedHours(
      Long employeeId, LoggedHoursCreateRequest req, Long callerAdminId) {
    ensureRequiredFieldsPresent(req);
    validateTimeRange(req.getStartTime(), req.getEndTime());
    validateNotFuture(req.getDate());

    EmployeeSummaryResponse employee = requireAdminAccessToEmployee(callerAdminId, employeeId);
    requireActive(employee);

    MergedInterval merged =
        mergeIntervalsForDay(employeeId, req.getDate(), req.getStartTime(), req.getEndTime(), null);

    LoggedHours saved =
        loggedHoursRepository.save(
            new LoggedHours(
                employeeId, callerAdminId, req.getDate(), merged.start(), merged.end()));
    return toLoggedHoursResponse(saved);
  }

  @Transactional(readOnly = true)
  public LoggedHoursListResponse listLoggedHours(Long employeeId, Long callerId, boolean isAdmin) {
    if (isAdmin) {
      requireAdminAccessToEmployee(callerId, employeeId);
    } else if (!callerId.equals(employeeId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this employee");
    }

    return new LoggedHoursListResponse(
        loggedHoursRepository.findAllByEmployeeIdOrderByDateDescStartTimeDesc(employeeId).stream()
            .map(this::toLoggedHoursResponse)
            .toList());
  }

  private EmployeeSummaryResponse findEmployeeOrThrow(Long employeeId) {
    return employeeApi
        .findSummaryById(employeeId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
  }

  private EmployeeSummaryResponse requireAdminAccessToEmployee(Long adminId, Long employeeId) {
    EmployeeSummaryResponse employee = findEmployeeOrThrow(employeeId);
    if (!adminApi.overseesDepartment(adminId, employee.getDepartmentId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this employee");
    }
    return employee;
  }

  private void ensureRequiredFieldsPresent(LoggedHoursCreateRequest req) {
    if (req.getDate() == null || req.getStartTime() == null || req.getEndTime() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "date, startTime and endTime are required");
    }
  }

  private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
    if (!startTime.isBefore(endTime)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Start time must be before end time");
    }
  }

  private void validateNotFuture(LocalDate date) {
    if (date.isAfter(LocalDate.now())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot log hours for a future date");
    }
  }

  private void requireActive(EmployeeSummaryResponse employee) {
    if (!employee.isActive()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee is not active");
    }
  }

  LoggedHoursResponse toLoggedHoursResponse(LoggedHours loggedHours) {
    return new LoggedHoursResponse(
        loggedHours.getId(),
        loggedHours.getEmployeeId(),
        loggedHours.getAdminId(),
        loggedHours.getDate(),
        loggedHours.getStartTime(),
        loggedHours.getEndTime());
  }
}
