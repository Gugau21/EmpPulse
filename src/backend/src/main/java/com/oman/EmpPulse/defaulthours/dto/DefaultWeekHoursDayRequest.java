package com.oman.EmpPulse.defaulthours.dto;

import java.util.List;

public class DefaultWeekHoursDayRequest {
  private Integer dayOfWeek;
  private List<TimeIntervalRequest> intervals;

  public Integer getDayOfWeek() {
    return dayOfWeek;
  }

  public void setDayOfWeek(Integer dayOfWeek) {
    this.dayOfWeek = dayOfWeek;
  }

  public List<TimeIntervalRequest> getIntervals() {
    return intervals;
  }

  public void setIntervals(List<TimeIntervalRequest> intervals) {
    this.intervals = intervals;
  }
}
