package com.oman.EmpPulse.defaulthours.dto;

import java.time.LocalTime;

public class TimeIntervalResponse {
  private LocalTime startTime;
  private LocalTime endTime;

  public TimeIntervalResponse(LocalTime startTime, LocalTime endTime) {
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
