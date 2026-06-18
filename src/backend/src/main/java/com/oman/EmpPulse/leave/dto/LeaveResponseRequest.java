package com.oman.EmpPulse.leave.dto;

import com.oman.EmpPulse.leave.api.LeaveStatus;

public class LeaveResponseRequest {
  private LeaveStatus status;
  private String adminComment;

  public LeaveStatus getStatus() {
    return status;
  }

  public void setStatus(LeaveStatus status) {
    this.status = status;
  }

  public String getAdminComment() {
    return adminComment;
  }

  public void setAdminComment(String adminComment) {
    this.adminComment = adminComment;
  }
}
