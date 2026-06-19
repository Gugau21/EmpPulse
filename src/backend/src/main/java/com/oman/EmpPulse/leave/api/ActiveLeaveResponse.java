package com.oman.EmpPulse.leave.api;

import java.time.LocalDate;

public class ActiveLeaveResponse {
  private LeaveType type;
  private LocalDate startDate;
  private LocalDate endDate;

  public ActiveLeaveResponse(LeaveType type, LocalDate startDate, LocalDate endDate) {
    this.type = type;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public LeaveType getType() {
    return type;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }
}
