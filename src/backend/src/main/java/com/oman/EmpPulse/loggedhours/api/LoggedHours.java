package com.oman.EmpPulse.loggedhours.api;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "logged_hours")
public class LoggedHours {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "employee_id", nullable = false)
  private Long employeeId;

  @Column(name = "admin_id", nullable = false)
  private Long adminId;

  @Column(nullable = false)
  private LocalDate date;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  public LoggedHours() {}

  public LoggedHours(
      Long employeeId, Long adminId, LocalDate date, LocalTime startTime, LocalTime endTime) {
    this.employeeId = employeeId;
    this.adminId = adminId;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public Long getId() {
    return id;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public Long getAdminId() {
    return adminId;
  }

  public LocalDate getDate() {
    return date;
  }

  public LocalDate setDate(LocalDate newDate) {
    return this.date = newDate;
  }

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
