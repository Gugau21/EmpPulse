package com.oman.EmpPulse.defaulthours.api;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "schedule_block")
public class ScheduleBlock {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "set_id", nullable = false)
  private Long setId;

  @Column(name = "day_of_week", nullable = false)
  private int dayOfWeek;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  public ScheduleBlock() {}

  public ScheduleBlock(Long setId, int dayOfWeek, LocalTime startTime, LocalTime endTime) {
    this.setId = setId;
    this.dayOfWeek = dayOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public Long getId() {
    return id;
  }

  public Long getSetId() {
    return setId;
  }

  public int getDayOfWeek() {
    return dayOfWeek;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }
}
