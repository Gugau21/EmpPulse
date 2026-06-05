package com.oman.EmpPulse.dto.response;

import java.util.List;

public class EmployeeListResponse {
    private List<EmployeeSummaryResponse> items;

    public EmployeeListResponse(List<EmployeeSummaryResponse> items) {
        this.items = items;
    }

    public List<EmployeeSummaryResponse> getItems() { return items; }
}
