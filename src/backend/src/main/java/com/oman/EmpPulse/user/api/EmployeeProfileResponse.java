package com.oman.EmpPulse.user.api;

import com.oman.EmpPulse.leave.api.ActiveLeaveResponse;

public class EmployeeProfileResponse {
  private Long employeeId;
  private Long departmentId;
  private String departmentName;
  private int yearlyVacationBalance;
  private int vacationBalance;
  private ActiveLeaveResponse activeLeave;

  public EmployeeProfileResponse(
      Long employeeId,
      Long departmentId,
      String departmentName,
      int yearlyVacationBalance,
      int vacationBalance,
      ActiveLeaveResponse activeLeave) {
    this.employeeId = employeeId;
    this.departmentId = departmentId;
    this.departmentName = departmentName;
    this.yearlyVacationBalance = yearlyVacationBalance;
    this.vacationBalance = vacationBalance;
    this.activeLeave = activeLeave;
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

  public int getVacationBalance() {
    return vacationBalance;
  }

  public ActiveLeaveResponse getActiveLeave() {
    return activeLeave;
  }
}
