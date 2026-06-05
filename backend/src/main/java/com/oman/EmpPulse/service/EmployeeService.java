package com.oman.EmpPulse.service;

import com.oman.EmpPulse.dto.response.EmployeeListResponse;
import com.oman.EmpPulse.dto.response.EmployeeSummaryResponse;
import com.oman.EmpPulse.entity.Admin;
import com.oman.EmpPulse.entity.AdminDepartment;
import com.oman.EmpPulse.entity.Department;
import com.oman.EmpPulse.entity.Employee;
import com.oman.EmpPulse.entity.User;
import com.oman.EmpPulse.repository.AdminDepartmentRepository;
import com.oman.EmpPulse.repository.AdminRepository;
import com.oman.EmpPulse.repository.DepartmentRepository;
import com.oman.EmpPulse.repository.EmployeeRepository;
import com.oman.EmpPulse.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AdminRepository adminRepository;
    private final AdminDepartmentRepository adminDepartmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           AdminRepository adminRepository,
                           AdminDepartmentRepository adminDepartmentRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.adminRepository = adminRepository;
        this.adminDepartmentRepository = adminDepartmentRepository;
    }

    public EmployeeListResponse getAllEmployees() {
        return toListResponse(employeeRepository.findAll());
    }

    public EmployeeListResponse getEmployeesForAdmin(Long userId) {
        Admin admin = adminRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
        List<Long> deptIds = adminDepartmentRepository.findByAdminId(admin.getId())
                .stream().map(AdminDepartment::getDepartmentId).toList();
        if (deptIds.isEmpty()) {
            return new EmployeeListResponse(List.of());
        }
        return toListResponse(employeeRepository.findByDepartmentIdIn(deptIds));
    }

    private EmployeeListResponse toListResponse(List<Employee> employees) {
        List<EmployeeSummaryResponse> items = employees.stream()
                .map(this::toEmployeeSummaryResponse)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EmployeeSummaryResponse::getSurname,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        return new EmployeeListResponse(items);
    }

    /** Returns null when the employee's user is missing or soft-deleted, so callers can filter it out. */
    private EmployeeSummaryResponse toEmployeeSummaryResponse(Employee employee) {
        User user = userRepository.findById(employee.getUserId()).orElse(null);
        if (user == null || user.isDeleted()) {
            return null;
        }
        String deptName = null;
        if (employee.getDepartmentId() != null) {
            deptName = departmentRepository.findById(employee.getDepartmentId())
                    .map(Department::getName).orElse(null);
        }
        return new EmployeeSummaryResponse(employee.getUserId(), user.getName(), user.getSurname(),
                employee.getDepartmentId(), deptName);
    }
}
