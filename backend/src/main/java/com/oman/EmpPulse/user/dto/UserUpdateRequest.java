package com.oman.EmpPulse.user.dto;

import java.util.List;

public class UserUpdateRequest {
  private String name;
  private String surname;
  private String email;
  private String password;

  /**
   * The employee's department change. Only applied when {@link #changeEmployeeDepartment} is true,
   * which is how we distinguish "field omitted, leave unchanged" from "set to a value / detach". A
   * {@code null} value with the flag set means detach from the current department.
   */
  private Long employeeDepartmentId;

  private boolean changeEmployeeDepartment;
  private Integer yearlyVacationBalance;
  private List<BonusVacationDayRequest> bonusVacationDays;
  private List<Long> adminDepartmentIds;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Long getEmployeeDepartmentId() {
    return employeeDepartmentId;
  }

  public void setEmployeeDepartmentId(Long employeeDepartmentId) {
    this.employeeDepartmentId = employeeDepartmentId;
  }

  public boolean hasChangeEmployeeDepartment() {
    return changeEmployeeDepartment;
  }

  public void setChangeEmployeeDepartment(boolean changeEmployeeDepartment) {
    this.changeEmployeeDepartment = changeEmployeeDepartment;
  }

  public Integer getYearlyVacationBalance() {
    return yearlyVacationBalance;
  }

  public void setYearlyVacationBalance(Integer yearlyVacationBalance) {
    this.yearlyVacationBalance = yearlyVacationBalance;
  }

  public List<BonusVacationDayRequest> getBonusVacationDays() {
    return bonusVacationDays;
  }

  public void setBonusVacationDays(List<BonusVacationDayRequest> bonusVacationDays) {
    this.bonusVacationDays = bonusVacationDays;
  }

  public List<Long> getAdminDepartmentIds() {
    return adminDepartmentIds;
  }

  public void setAdminDepartmentIds(List<Long> adminDepartmentIds) {
    this.adminDepartmentIds = adminDepartmentIds;
  }
}
