package com.oman.EmpPulse.dto.response;

import java.util.List;

public class DepartmentListResponse {
    private List<DepartmentResponse> items;

    public DepartmentListResponse(List<DepartmentResponse> items) {
        this.items = items;
    }

    public List<DepartmentResponse> getItems() { return items; }
}
