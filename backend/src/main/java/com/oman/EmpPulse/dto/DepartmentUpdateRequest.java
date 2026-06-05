package com.oman.EmpPulse.dto;

import java.util.List;

public class DepartmentUpdateRequest {
    private String name;
    private List<Long> adminIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Long> getAdminIds() { return adminIds; }
    public void setAdminIds(List<Long> adminIds) { this.adminIds = adminIds; }
}
