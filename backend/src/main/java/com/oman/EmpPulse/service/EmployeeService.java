package com.oman.EmpPulse.service;

import com.oman.EmpPulse.dto.response.EmployeeListResponse;
import com.oman.EmpPulse.dto.response.EmployeeSummaryResponse;
import com.oman.EmpPulse.entity.Department;
import com.oman.EmpPulse.entity.Employee;
import com.oman.EmpPulse.entity.User;
import com.oman.EmpPulse.repository.DepartmentRepository;
import com.oman.EmpPulse.repository.EmployeeRepository;
import com.oman.EmpPulse.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final ServiceUtils serviceUtils;

  public EmployeeService(
      EmployeeRepository employeeRepository,
      UserRepository userRepository,
      DepartmentRepository departmentRepository,
      ServiceUtils serviceUtils) {
    this.employeeRepository = employeeRepository;
    this.userRepository = userRepository;
    this.departmentRepository = departmentRepository;
    this.serviceUtils = serviceUtils;
  }

  public EmployeeListResponse getAllEmployees() {
    return toListResponse(employeeRepository.findAll());
  }

  public EmployeeListResponse getEmployeesForAdmin(Long userId) {
    List<Long> deptIds = serviceUtils.getDeptIdsForAdminUser(userId);
    if (deptIds.isEmpty()) {
      return new EmployeeListResponse(List.of());
    }
    return toListResponse(employeeRepository.findByDepartmentIdIn(deptIds));
  }

  private EmployeeListResponse toListResponse(List<Employee> employees) {
    List<EmployeeSummaryResponse> items =
        employees.stream()
            .map(this::toEmployeeSummaryResponse)
            .filter(Objects::nonNull)
            .sorted(
                Comparator.comparing(
                    EmployeeSummaryResponse::getSurname,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
            .toList();
    return new EmployeeListResponse(items);
  }

  /**
   * Returns null when the employee's user is missing or soft-deleted, so callers can filter it out.
   */
  private EmployeeSummaryResponse toEmployeeSummaryResponse(Employee employee) {
    User user = userRepository.findById(employee.getUserId()).orElse(null);
    if (user == null || user.isDeleted()) {
      return null;
    }
    String deptName = null;
    if (employee.getDepartmentId() != null) {
      deptName =
          departmentRepository
              .findById(employee.getDepartmentId())
              .map(Department::getName)
              .orElse(null);
    }
    return new EmployeeSummaryResponse(
        employee.getUserId(),
        user.getName(),
        user.getSurname(),
        employee.getDepartmentId(),
        deptName);
  }
}
