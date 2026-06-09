package com.oman.EmpPulse.user.internal;

import jakarta.persistence.*;

@Entity
@Table(name = "employee")
public class Employee {

  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "department_id")
  private Long departmentId;

  @Column(name = "week_schedule_id")
  private Long weekScheduleId;

  @Column(name = "vacation_balance", nullable = false)
  private int vacationBalance;

  @Column(name = "active", nullable = false, insertable = false, updatable = false)
  private boolean active;

  public Employee() {}

  public Employee(Long id, Long departmentId, Long weekScheduleId, int vacationBalance) {
    this.id = id;
    this.departmentId = departmentId;
    this.weekScheduleId = weekScheduleId;
    this.vacationBalance = vacationBalance;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(Long departmentId) {
    this.departmentId = departmentId;
  }

  public Long getWeekScheduleId() {
    return weekScheduleId;
  }

  public void setWeekScheduleId(Long weekScheduleId) {
    this.weekScheduleId = weekScheduleId;
  }

  public int getVacationBalance() {
    return vacationBalance;
  }

  public void setVacationBalance(int vacationBalance) {
    this.vacationBalance = vacationBalance;
  }

  public boolean isActive() {
    return active;
  }
}
