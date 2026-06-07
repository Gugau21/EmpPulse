package com.oman.EmpPulse.user.dto;

import java.util.List;

public class UserUpdateRequest {
  private String name;
  private String surname;
  private String email;
  private String password;
  private Long employeeDepartmentId;
  private Integer yearlyVacationBalance;
  private List<PremiumVacationDayRequest> premiumVacationDays;
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

  public Integer getYearlyVacationBalance() {
    return yearlyVacationBalance;
  }

  public void setYearlyVacationBalance(Integer yearlyVacationBalance) {
    this.yearlyVacationBalance = yearlyVacationBalance;
  }

  public List<PremiumVacationDayRequest> getPremiumVacationDays() {
    return premiumVacationDays;
  }

  public void setPremiumVacationDays(List<PremiumVacationDayRequest> premiumVacationDays) {
    this.premiumVacationDays = premiumVacationDays;
  }

  public List<Long> getAdminDepartmentIds() {
    return adminDepartmentIds;
  }

  public void setAdminDepartmentIds(List<Long> adminDepartmentIds) {
    this.adminDepartmentIds = adminDepartmentIds;
  }
}
