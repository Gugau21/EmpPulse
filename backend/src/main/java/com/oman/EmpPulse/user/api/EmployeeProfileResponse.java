package com.oman.EmpPulse.user.api;

import java.util.List;

public class EmployeeProfileResponse {
  private Long employeeId;
  private Long departmentId;
  private String departmentName;
  private int yearlyVacationBalance;
  private List<Object> bonusVacationDays;

  public EmployeeProfileResponse(
      Long employeeId, Long departmentId, String departmentName, int yearlyVacationBalance) {
    this.employeeId = employeeId;
    this.departmentId = departmentId;
    this.departmentName = departmentName;
    this.yearlyVacationBalance = yearlyVacationBalance;
    this.bonusVacationDays = List.of();
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public Long getDepartmentId() {
    return departmentId;
  }

  public String getDepartmentName() {
    return departmentName;
  }

  public int getYearlyVacationBalance() {
    return yearlyVacationBalance;
  }

  public List<Object> getBonusVacationDays() {
    return bonusVacationDays;
  }
}
