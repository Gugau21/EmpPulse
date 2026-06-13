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
}
