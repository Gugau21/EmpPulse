package com.oman.EmpPulse.leave.dto;

import com.oman.EmpPulse.leave.internal.LeaveType;
import java.time.LocalDate;

public class LeaveCreateRequest {
  private Long employeeId;
  private LeaveType type;
  private Boolean paid;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private String adminComment;

  public Long getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(Long employeeId) {
    this.employeeId = employeeId;
  }

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
