package com.oman.EmpPulse.user.api;

public class EmployeeSummaryResponse {
  private Long id;
  private String name;
  private String surname;
  private Long departmentId;
  private String departmentName;
  private boolean active;

  public EmployeeSummaryResponse(
      Long id,
      String name,
      String surname,
      Long departmentId,
      String departmentName,
      boolean active) {
    this.id = id;
    this.name = name;
    this.surname = surname;
    this.departmentId = departmentId;
    this.departmentName = departmentName;
    this.active = active;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSurname() {
    return surname;
  }

  public Long getDepartmentId() {
    return departmentId;
  }

  public String getDepartmentName() {
    return departmentName;
  }

  public boolean isActive() {
    return active;
  }
}
