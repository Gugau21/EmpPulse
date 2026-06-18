package com.oman.EmpPulse.defaulthours.dto;

import java.util.List;

public class DefaultWeekHoursDayResponse {
  private int dayOfWeek;
  private List<TimeIntervalResponse> intervals;

  public DefaultWeekHoursDayResponse(int dayOfWeek, List<TimeIntervalResponse> intervals) {
    this.dayOfWeek = dayOfWeek;
    this.intervals = intervals;
  }

  public int getDayOfWeek() {
    return dayOfWeek;
  }

  public List<TimeIntervalResponse> getIntervals() {
    return intervals;
  }
}
