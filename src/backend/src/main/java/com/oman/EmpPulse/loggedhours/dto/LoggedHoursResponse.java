package com.oman.EmpPulse.loggedhours.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class LoggedHoursResponse {
  private Long id;
  private Long employeeId;
  private Long adminId;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;

  public LoggedHoursResponse(
      Long id,
      Long employeeId,
      Long adminId,
      LocalDate date,
      LocalTime startTime,
      LocalTime endTime) {
    this.id = id;
    this.employeeId = employeeId;
    this.adminId = adminId;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public Long getId() {
    return id;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public Long getAdminId() {
    return adminId;
  }

  public LocalDate getDate() {
    return date;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }
}
