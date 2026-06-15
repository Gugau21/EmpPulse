package com.oman.EmpPulse.loggedhours.internal;

import com.oman.EmpPulse.loggedhours.api.LoggedHoursApi;
import com.oman.EmpPulse.loggedhours.dto.LoggedHoursCreateRequest;
import com.oman.EmpPulse.loggedhours.dto.LoggedHoursListResponse;
import com.oman.EmpPulse.loggedhours.dto.LoggedHoursResponse;
import com.oman.EmpPulse.loggedhours.dto.LoggedHoursUpdateRequest;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Collection;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoggedHoursService implements LoggedHoursApi {

  private final LoggedHoursRepository loggedHoursRepository;
  private final EmployeeApi employeeApi;
  private final AdminApi adminApi;

  public LoggedHoursService(
      LoggedHoursRepository loggedHoursRepository, EmployeeApi employeeApi, AdminApi adminApi) {
    this.loggedHoursRepository = loggedHoursRepository;
    this.employeeApi = employeeApi;
    this.adminApi = adminApi;
  }

  @Override
  @Transactional
  public void deleteByEmployeeAndDateRange(
      Long employeeId, LocalDate startDate, LocalDate endDate) {
    loggedHoursRepository.deleteAll(
        loggedHoursRepository.findAllByEmployeeIdAndDateBetween(employeeId, startDate, endDate));
  }

  @Override
  @Transactional(readOnly = true)
  public List<LoggedHours> findAllByEmployeeIdIn(Collection<Long> employeeIds) {
    return loggedHoursRepository.findAllByEmployeeIdInOrderByDateDescStartTimeDesc(employeeIds);
  }

  private record MergedInterval(LocalTime start, LocalTime end) {}

  /**
   * Scans all logged-hours rows for {@code employeeId} on {@code date} (excluding {@code excludeId}
   * if non-null), finds every row that overlaps or is adjacent to {@code [newStart, newEnd]},
   * deletes those rows, and returns the widest interval that covers the input and all matched rows.
   *
   * <p>Callers must save the resulting interval themselves. Passing {@code excludeId} for PATCH
   * prevents the row being updated from merging with itself.
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
    ensureRequiredFieldsPresent(req.getDate(), req.getStartTime(), req.getEndTime());
    validateTimeRange(req.getStartTime(), req.getEndTime());
    validateNotFuture(req.getDate());
    requireAdminAccessToEmployee(callerAdminId, employeeId);

    MergedInterval merged =
        mergeIntervalsForDay(employeeId, req.getDate(), req.getStartTime(), req.getEndTime(), null);

    LoggedHours saved =
        loggedHoursRepository.save(
            new LoggedHours(
                employeeId, callerAdminId, req.getDate(), merged.start(), merged.end()));
    return toLoggedHoursResponse(saved);
  }

  /**
   * Returns all logged hours for {@code employeeId}, sorted by date and start time descending.
   *
   * @param employeeId the employee whose hours to list
   * @param callerId the user ID of the authenticated caller
   * @param isAdmin true if the caller holds the ADMIN role
   * @return list of logged hours entries
   */
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

  /**
   * Updates date/time of an existing logged-hours entry, merging adjacent/overlapping intervals
   * (same as createLoggedHours)
   *
   * @param employeeId the employee the hours belong to
   * @param loggedHoursId the entry to update
   * @param req new date, start time, and end time
   * @param callerId the user ID of the authenticated admin
   * @return the updated logged hours entry
   */
  @Transactional
  public LoggedHoursResponse updateLoggedHours(
      Long employeeId, Long loggedHoursId, LoggedHoursUpdateRequest req, Long callerId) {
    requireAdminAccessToEmployee(callerId, employeeId);
    ensureRequiredFieldsPresent(req.getDate(), req.getStartTime(), req.getEndTime());
    validateTimeRange(req.getStartTime(), req.getEndTime());
    validateNotFuture(req.getDate());

    LoggedHours loggedHoursToUpdate = findLoggedHoursForEmployeeOrThrow(loggedHoursId, employeeId);

    MergedInterval mergedInterval =
        mergeIntervalsForDay(
            employeeId, req.getDate(), req.getStartTime(), req.getEndTime(), loggedHoursId);

    loggedHoursToUpdate.setDate(req.getDate());
    loggedHoursToUpdate.setStartTime(mergedInterval.start());
    loggedHoursToUpdate.setEndTime(mergedInterval.end());
    loggedHoursRepository.save(loggedHoursToUpdate);

    return toLoggedHoursResponse(loggedHoursToUpdate);
  }

  /**
   * Hard deletes a logged-hours entry on behalf of an admin.
   *
   * @param employeeId the employee the hours belong to
   * @param loggedHoursId the logged-hours entry to delete
   * @param callerId the user ID of the authenticated admin
   */
  @Transactional
  public void deleteLoggedHours(Long employeeId, Long loggedHoursId, Long callerId) {
    requireAdminAccessToEmployee(callerId, employeeId);
    LoggedHours loggedHours = findLoggedHoursForEmployeeOrThrow(loggedHoursId, employeeId);
    loggedHoursRepository.delete(loggedHours);
  }

  private LoggedHours findLoggedHoursForEmployeeOrThrow(Long loggedHoursId, Long employeeId) {
    LoggedHours loggedHours =
        loggedHoursRepository
            .findById(loggedHoursId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Logged hours entry not found"));
    if (!loggedHours.getEmployeeId().equals(employeeId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Logged hours entry not found");
    }
    return loggedHours;
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

  private void ensureRequiredFieldsPresent(LocalDate date, LocalTime startTime, LocalTime endTime) {
    if (date == null || startTime == null || endTime == null) {
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
