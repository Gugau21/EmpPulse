package com.oman.EmpPulse.user.api;

import com.oman.EmpPulse.leave.api.ActiveLeaveResponse;
import java.util.List;

public class EmployeeProfileResponse {
  private Long employeeId;
  private Long departmentId;
  private String departmentName;
  private int yearlyVacationBalance;
  private List<Object> bonusVacationDays;
  private ActiveLeaveResponse activeLeave;

  public EmployeeProfileResponse(
      Long employeeId,
      Long departmentId,
      String departmentName,
      int yearlyVacationBalance,
      ActiveLeaveResponse activeLeave) {
    this.employeeId = employeeId;
    this.departmentId = departmentId;
    this.departmentName = departmentName;
    this.yearlyVacationBalance = yearlyVacationBalance;
    this.bonusVacationDays = List.of();
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

  public List<Object> getBonusVacationDays() {
    return bonusVacationDays;
  }

  public ActiveLeaveResponse getActiveLeave() {
    return activeLeave;
  }
}
