package com.oman.EmpPulse.user.api;

import jakarta.persistence.*;

@Entity
@Table(name = "bonus_vacation_days")
public class BonusVacationDays {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "employee_id", nullable = false)
  private Long employeeId;

  @Column(name = "year_", nullable = false)
  private int year;

  @Column(name = "days", nullable = false)
  private int days;

  public BonusVacationDays() {}

  public BonusVacationDays(Long employeeId, int year, int days) {
    this.employeeId = employeeId;
    this.year = year;
    this.days = days;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(Long employeeId) {
    this.employeeId = employeeId;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getDays() {
    return days;
  }

  public void setDays(int days) {
    this.days = days;
  }
}
