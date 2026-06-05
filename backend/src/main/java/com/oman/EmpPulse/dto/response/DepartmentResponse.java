package com.oman.EmpPulse.dto.response;

import java.util.List;

public class DepartmentResponse {
    private Long id;
    private String name;
    private List<AdminSummaryResponse> admins;

    public DepartmentResponse(Long id, String name, List<AdminSummaryResponse> admins) {
        this.id = id;
        this.name = name;
        this.admins = admins;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<AdminSummaryResponse> getAdmins() { return admins; }
}
