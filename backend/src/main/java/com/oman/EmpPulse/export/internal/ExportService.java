package com.oman.EmpPulse.export.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.shared.security.AuthUtils;
import com.oman.EmpPulse.user.api.AdminApi;

@Service
public class ExportService {

    private final DepartmentApi departmentApi;
    private final CsvExportService csvExportService;
    private final ZipService zipService;
    private final AdminApi adminApi;

    public ExportService(DepartmentApi departmentApi,
                         AdminApi adminApi,
                         CsvExportService csvExportService,
                         ZipService zipService) {
        this.departmentApi = departmentApi;
        this.adminApi = adminApi;
        this.csvExportService = csvExportService;
        this.zipService = zipService;
    }

    public byte[] exportDepartments(Authentication authentication) {
        List<Department> departments = isOwner(authentication)
            ? new ArrayList<Department>(departmentApi.findAll())
            : new ArrayList<Department>(departmentApi.findAllByIds(adminApi.departmentIdsForAdminUser(AuthUtils.getUserId(authentication))));

        byte[] csv = csvExportService.toCsv(departments);
        return zipService.zip(Map.of("departments.csv", csv));
    }

    private boolean isOwner(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("OWNER"));
    }
}