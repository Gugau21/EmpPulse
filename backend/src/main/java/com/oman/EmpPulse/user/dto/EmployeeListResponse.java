package com.oman.EmpPulse.user.dto;

import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import java.util.List;

public class EmployeeListResponse {
  private List<EmployeeSummaryResponse> items;

  public EmployeeListResponse(List<EmployeeSummaryResponse> items) {
    this.items = items;
  }

  public List<EmployeeSummaryResponse> getItems() {
    return items;
  }
}
