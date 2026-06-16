package com.oman.EmpPulse.defaulthours.internal;

import jakarta.persistence.*;

@Entity
@Table(name = "week_schedule")
public class WeekSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public WeekSchedule() {}

  public Long getId() {
    return id;
  }
}
