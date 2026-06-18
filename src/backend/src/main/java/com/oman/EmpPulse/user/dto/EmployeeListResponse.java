package com.oman.EmpPulse.user.dto;

import java.util.List;

public class EmployeeListResponse {
  private List<EmployeeListItemResponse> items;

  public EmployeeListResponse(List<EmployeeListItemResponse> items) {
    this.items = items;
  }

  public List<EmployeeListItemResponse> getItems() {
    return items;
  }
}
