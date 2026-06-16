package com.oman.EmpPulse.defaulthours.dto;

import java.util.List;

public class DefaultWeekHoursRequest {
  private List<DefaultWeekHoursDayRequest> days;

  public List<DefaultWeekHoursDayRequest> getDays() {
    return days;
  }

  public void setDays(List<DefaultWeekHoursDayRequest> days) {
    this.days = days;
  }
}
