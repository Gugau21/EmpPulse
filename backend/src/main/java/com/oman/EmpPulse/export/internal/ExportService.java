package com.oman.EmpPulse.export.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.shared.security.AuthUtils;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.internal.EmployeeRepository;
import com.oman.EmpPulse.user.internal.Employee;

@Service
public class ExportService {

    private final DepartmentApi departmentApi;
    private final CsvExportService csvExportService;
    private final ZipService zipService;
    private final AdminApi adminApi;
    private final EmployeeRepository employeeRepository;

    public ExportService(DepartmentApi departmentApi,
                         AdminApi adminApi,
                         CsvExportService csvExportService,
                         ZipService zipService,
                         EmployeeRepository employeeRepository) {
        this.departmentApi = departmentApi;
        this.adminApi = adminApi;
        this.csvExportService = csvExportService;
        this.zipService = zipService;
        this.employeeRepository = employeeRepository;
    }

    public byte[] export(Authentication authentication) {
        List<Department> departments = isOwner(authentication)
            ? new ArrayList<Department>(departmentApi.findAll())
            : new ArrayList<Department>(departmentApi.findAllByIds(adminApi.departmentIdsForAdminUser(AuthUtils.getUserId(authentication))));
        List<Employee> employees = employeeRepository.findByDepartmentIdIn(departments.stream().map(Department::getId).toList());
        Map<String, byte[]> files = new HashMap<>();
        files.put("departments.csv", csvExportService.departmentsToCsv(departments));
        files.put("employees.csv", csvExportService.employeesToCsv(employees));
        return zipService.zip(files);
    }

    private boolean isOwner(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("OWNER"));
    }
}