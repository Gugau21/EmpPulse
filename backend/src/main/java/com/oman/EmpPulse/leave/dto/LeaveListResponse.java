package com.oman.EmpPulse.leave.dto;

import java.util.List;

public class LeaveListResponse {
  private List<LeaveResponse> items;

  public LeaveListResponse(List<LeaveResponse> items) {
    this.items = items;
  }

  public List<LeaveResponse> getItems() {
    return items;
  }
}
