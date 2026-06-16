package com.oman.EmpPulse.defaulthours.dto;

import java.time.LocalTime;

public class TimeIntervalRequest {
  private LocalTime startTime;
  private LocalTime endTime;

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }
}
