package com.oman.EmpPulse.loggedhours.dto;

import java.util.List;

public class LoggedHoursListResponse {

  private List<LoggedHoursResponse> items;

  public LoggedHoursListResponse(List<LoggedHoursResponse> items) {
    this.items = items;
  }

  public List<LoggedHoursResponse> getItems() {
    return items;
  }
}
