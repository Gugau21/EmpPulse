package com.oman.EmpPulse.defaulthours.api;

import java.time.LocalTime;

public class DefaultDayIntervalResponse {

  private final LocalTime startTime;
  private final LocalTime endTime;

  public DefaultDayIntervalResponse(LocalTime startTime, LocalTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }
}
