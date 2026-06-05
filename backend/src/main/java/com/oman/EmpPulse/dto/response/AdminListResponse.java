package com.oman.EmpPulse.dto.response;

import java.util.List;

public class AdminListResponse {
    private List<AdminSummaryResponse> items;

    public AdminListResponse(List<AdminSummaryResponse> items) {
        this.items = items;
    }

    public List<AdminSummaryResponse> getItems() { return items; }
}
