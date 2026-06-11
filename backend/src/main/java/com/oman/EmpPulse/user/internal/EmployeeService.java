package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import com.oman.EmpPulse.user.dto.EmployeeListResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

  @Override
  public boolean hasEmployeesInDepartment(Long departmentId) {
    return employeeRepository.existsByDepartmentId(departmentId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<EmployeeSummaryResponse> findSummaryById(Long employeeId) {
    return employeeRepository.findById(employeeId).map(this::toEmployeeSummaryResponse);
  }

  @Transactional(readOnly = true)
  public EmployeeListResponse getAllEmployees() {
    return toListResponse(employeeRepository.findByActiveTrue());
  }

  @Transactional(readOnly = true)
  public EmployeeListResponse getEmployeesForAdmin(Long id) {
    List<Long> deptIds = adminApi.departmentIdsForAdminUser(id);
    return toListResponse(employeeRepository.findByDepartmentIdIn(deptIds));
  }

  private EmployeeListResponse toListResponse(List<Employee> employees) {
    List<EmployeeSummaryResponse> items =
        employees.stream().map(this::toEmployeeSummaryResponse).toList();
    return new EmployeeListResponse(items);
  }

  private EmployeeSummaryResponse toEmployeeSummaryResponse(Employee employee) {
    User user =
        userRepository
            .findById(employee.getId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));
    String deptName = departmentApi.findNameById(employee.getDepartmentId()).orElse(null);

    return new EmployeeSummaryResponse(
        employee.getId(),
        user.getName(),
        user.getSurname(),
        employee.getDepartmentId(),
        deptName,
        employee.isActive());
  }
}
