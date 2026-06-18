package com.oman.EmpPulse.leave.dto;

import java.time.LocalDate;
import com.oman.EmpPulse.leave.api.LeaveType;

public class LeaveUpdateRequest {
  private LeaveType type;
  private Boolean paid;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private String adminComment;

  public LeaveType getType() {
    return type;
  }

  public void setType(LeaveType type) {
    this.type = type;
  }

  public Boolean getPaid() {
    return paid;
  }

  public void setPaid(Boolean paid) {
    this.paid = paid;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getAdminComment() {
    return adminComment;
  }

  public void setAdminComment(String adminComment) {
    this.adminComment = adminComment;
  }
}
