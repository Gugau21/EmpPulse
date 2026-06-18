package com.oman.EmpPulse.user.api;

import java.util.List;

public class AdminProfileResponse {
  private Long adminId;
  private List<Long> departmentIds;

  public AdminProfileResponse(Long adminId, List<Long> departmentIds) {
    this.adminId = adminId;
    this.departmentIds = departmentIds;
  }

  public Long getAdminId() {
    return adminId;
  }

  public List<Long> getDepartmentIds() {
    return departmentIds;
  }
}
