package com.oman.EmpPulse.loggedhours.api;

import java.time.LocalDate;

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
}
