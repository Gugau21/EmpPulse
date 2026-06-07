package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.dto.EmployeeListResponse;
import com.oman.EmpPulse.user.dto.EmployeeSummaryResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService implements EmployeeApi {

  private final EmployeeRepository employeeRepository;
  private final UserRepository userRepository;
  private final AdminApi adminApi;
  private final DepartmentApi departmentApi;

  public EmployeeService(
      EmployeeRepository employeeRepository,
      UserRepository userRepository,
      AdminApi adminApi,
      @Lazy DepartmentApi departmentApi) {
    this.employeeRepository = employeeRepository;
    this.userRepository = userRepository;
    this.adminApi = adminApi;
    this.departmentApi = departmentApi;
  }

  @Transactional(readOnly = true)
  public EmployeeListResponse getAllEmployees() {
    return toListResponse(employeeRepository.findAll());
  }

  @Transactional(readOnly = true)
  public EmployeeListResponse getEmployeesForAdmin(Long userId) {
    List<Long> deptIds = adminApi.departmentIdsForAdminUser(userId);
    if (deptIds.isEmpty()) {
      return new EmployeeListResponse(List.of());
    }
    return toListResponse(employeeRepository.findByDepartmentIdIn(deptIds));
  }

  @Override
  public boolean hasEmployeesInDepartment(Long departmentId) {
    return employeeRepository.existsByDepartmentId(departmentId);
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

  private EmployeeSummaryResponse toEmployeeSummaryResponse(Employee employee) {
    User user = userRepository.findById(employee.getUserId()).orElse(null);
    if (user == null || user.isDeleted()) {
      return null;
    }
    String deptName = null;
    if (employee.getDepartmentId() != null) {
      deptName = departmentApi.findNameById(employee.getDepartmentId()).orElse(null);
    }
    return new EmployeeSummaryResponse(
        employee.getUserId(),
        user.getName(),
        user.getSurname(),
        employee.getDepartmentId(),
        deptName);
  }
}
