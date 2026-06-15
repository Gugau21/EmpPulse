package com.oman.EmpPulse.export.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.leave.internal.Leave;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.shared.security.AuthUtils;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.internal.Employee;
import com.oman.EmpPulse.user.internal.User;
import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.Admin;

@Service
public class ExportService {

    private final DepartmentApi departmentApi;
    private final CsvExportService csvExportService;
    private final ZipService zipService;
    private final AdminApi adminApi;
    private final EmployeeApi employeeApi;
    private final UserApi userApi;
    private final LeaveApi leaveApi;

    public ExportService(DepartmentApi departmentApi,
                         AdminApi adminApi,
                         CsvExportService csvExportService,
                         ZipService zipService,
                         EmployeeApi employeeApi,
                         UserApi userApi,
                         LeaveApi leaveApi) {
        this.departmentApi = departmentApi;
        this.adminApi = adminApi;
        this.csvExportService = csvExportService;
        this.zipService = zipService;
        this.employeeApi = employeeApi;
        this.userApi = userApi;
        this.leaveApi = leaveApi;
    }

    public byte[] export(Authentication authentication) {
        List<Department> departments = isOwner(authentication)
            ? new ArrayList<Department>(departmentApi.findAll())
            : new ArrayList<Department>(departmentApi.findAllByIds(adminApi.departmentIdsForAdminUser(AuthUtils.getUserId(authentication))));
        List<Employee> employees = employeeApi.findAllByDepartmentIdIn(departments.stream().map(Department::getId).collect(Collectors.toSet())).stream().toList();
        List<Leave> leaves = leaveApi.findAllByEmployeeIdIn(employees.stream().map(Employee::getId).collect(Collectors.toSet())).stream().toList();
        List<Admin> admins = new ArrayList<Admin>();
        Set<User> users = userApi.findByIdIn(employees.stream().map(Employee::getId).collect(Collectors.toSet())).stream().collect(Collectors.toSet());
        users.addAll(userApi.findByIdIn(admins.stream().map(Admin::getId).collect(Collectors.toSet())));
        users.add(userApi.findById(AuthUtils.getUserId(authentication)).orElseThrow());
        Map<String, byte[]> files = new HashMap<>();
        files.put("departments.csv", csvExportService.departmentsToCsv(departments));
        files.put("employees.csv", csvExportService.employeesToCsv(employees));
        files.put("users.csv", csvExportService.usersToCsv(users));
        files.put("leaves.csv", csvExportService.leavesToCsv(leaves));
        return zipService.zip(files);
    }

    private boolean isOwner(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("OWNER"));
    }
}