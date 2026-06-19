package com.oman.EmpPulse.user.dto;

import com.oman.EmpPulse.leave.api.ActiveLeaveResponse;

public class EmployeeListItemResponse {
  private Long id;
  private String name;
  private String surname;
  private Long departmentId;
  private String departmentName;
  private boolean active;
  private ActiveLeaveResponse activeLeave;

  public EmployeeListItemResponse(
      Long id,
      String name,
      String surname,
      Long departmentId,
      String departmentName,
      boolean active,
      ActiveLeaveResponse activeLeave) {
    this.id = id;
    this.name = name;
    this.surname = surname;
    this.departmentId = departmentId;
    this.departmentName = departmentName;
    this.active = active;
    this.activeLeave = activeLeave;
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

  public ActiveLeaveResponse getActiveLeave() {
    return activeLeave;
  }
}
