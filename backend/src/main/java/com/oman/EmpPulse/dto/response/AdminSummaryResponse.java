package com.oman.EmpPulse.dto.response;

import java.util.List;

public class AdminSummaryResponse {
    private Long id;
    private UserSummaryResponse user;
    private List<Long> departmentIds;
    public AdminSummaryResponse(Long id, UserSummaryResponse user, List<Long> departmentIds) {
        this.id = id;
        this.user = user;
        this.departmentIds = departmentIds;
    }

    public Long getId() { return id; }
    public UserSummaryResponse getUser() { return user; }
    public List<Long> getDepartmentIds() { return departmentIds; }
}
