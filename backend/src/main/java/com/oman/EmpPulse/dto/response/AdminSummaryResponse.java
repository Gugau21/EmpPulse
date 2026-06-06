package com.oman.EmpPulse.dto.response;

import java.util.List;

public class AdminSummaryResponse {
  private Long adminId;
  private UserSummaryResponse user;
  private List<Long> departmentIds;

  public AdminSummaryResponse(Long adminId, UserSummaryResponse user, List<Long> departmentIds) {
    this.adminId = adminId;
    this.user = user;
    this.departmentIds = departmentIds;
  }

  public Long getId() {
    return adminId;
  }

  public UserSummaryResponse getUser() {
    return user;
  }

  public List<Long> getDepartmentIds() {
    return departmentIds;
  }
}
