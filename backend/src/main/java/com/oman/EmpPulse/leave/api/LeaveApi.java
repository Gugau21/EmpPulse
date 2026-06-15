package com.oman.EmpPulse.leave.api;

import java.util.Collection;
import java.util.Map;

public interface LeaveApi {

  /**
   * Finds each employee's currently active approved leave, if any.
   *
   * <p>Active means {@code startDate <= today <= endDate} and status is approved. Employees without
   * an active leave are absent from the result map.
   *
   * @param employeeIds the employee IDs to look up
   * @return a map from employee ID to their active leave snapshot
   */
  Map<Long, ActiveLeaveResponse> findActiveLeavesByEmployeeIds(Collection<Long> employeeIds);

  /**
   * Counts the working days (Monday–Friday) of the employee's active (pending or approved) Vacation
   * leave requests that fall within the given calendar year.
   *
   * <p>A leave request crossing a year boundary contributes only the portion within the requested
   * year. Weekends are excluded; the system has no holiday concept.
   *
   * @param employeeId the employee whose used vacation days to count
   * @param year the calendar year to count within
   * @return the number of used vacation working days in that year
   */
  int countUsedVacationDays(Long employeeId, int year);
}
