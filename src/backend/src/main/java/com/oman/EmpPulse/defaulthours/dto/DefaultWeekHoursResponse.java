package com.oman.EmpPulse.defaulthours.dto;

import java.util.List;

public class DefaultWeekHoursResponse {
  private List<DefaultWeekHoursDayResponse> days;

  public DefaultWeekHoursResponse(List<DefaultWeekHoursDayResponse> days) {
    this.days = days;
  }

  public List<DefaultWeekHoursDayResponse> getDays() {
    return days;
  }
}
