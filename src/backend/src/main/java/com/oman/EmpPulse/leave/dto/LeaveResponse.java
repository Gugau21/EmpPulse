package com.oman.EmpPulse.leave.dto;

import com.oman.EmpPulse.leave.internal.LeaveStatus;
import com.oman.EmpPulse.leave.internal.LeaveType;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class LeaveResponse {
  private Long id;
  private EmployeeSummaryResponse employee;
  private LeaveType type;
  private boolean paid;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private LeaveStatus status;
  private Long adminReviewerId;
  private String adminComment;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public LeaveResponse(
      Long id,
      EmployeeSummaryResponse employee,
      LeaveType type,
      boolean paid,
      LocalDate startDate,
      LocalDate endDate,
      String reason,
      LeaveStatus status,
      Long adminReviewerId,
      String adminComment,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    this.id = id;
    this.employee = employee;
    this.type = type;
    this.paid = paid;
    this.startDate = startDate;
    this.endDate = endDate;
    this.reason = reason;
    this.status = status;
    this.adminReviewerId = adminReviewerId;
    this.adminComment = adminComment;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public EmployeeSummaryResponse getEmployee() {
    return employee;
  }

  public LeaveType getType() {
    return type;
  }

  public boolean isPaid() {
    return paid;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public String getReason() {
    return reason;
  }

  public LeaveStatus getStatus() {
    return status;
  }

  public Long getAdminReviewerId() {
    return adminReviewerId;
  }

  public String getAdminComment() {
    return adminComment;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
