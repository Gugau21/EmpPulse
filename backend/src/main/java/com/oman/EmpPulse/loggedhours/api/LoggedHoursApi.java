package com.oman.EmpPulse.loggedhours.api;

import java.util.List;
import java.time.LocalDate;
import java.util.Collection;
import com.oman.EmpPulse.loggedhours.internal.LoggedHours;

public interface LoggedHoursApi {

  /**
   * Deletes all logged hours of the employee whose date falls within {@code [startDate, endDate]}
   * (inclusive).
   *
   * @param employeeId the employee whose hours to delete
   * @param startDate the first date of the range (inclusive)
   * @param endDate the last date of the range (inclusive)
   */
  void deleteByEmployeeAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate);

  /**
   * Finds all logged hours of the employees whose IDs are in {@code employeeIds}.
   *
   * @param employeeIds the IDs of the employees whose logged hours to find
   * @return a list of logged hours of the specified employees
   */
  List<LoggedHours> findAllByEmployeeIdIn(Collection<Long> employeeIds);
}
