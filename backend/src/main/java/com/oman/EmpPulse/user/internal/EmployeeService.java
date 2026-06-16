package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.leave.api.ActiveLeaveResponse;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import com.oman.EmpPulse.user.dto.EmployeeListItemResponse;
import com.oman.EmpPulse.user.dto.EmployeeListResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
  private final LeaveApi leaveApi;

  public EmployeeService(
      EmployeeRepository employeeRepository,
      UserRepository userRepository,
      AdminApi adminApi,
      @Lazy DepartmentApi departmentApi,
      @Lazy LeaveApi leaveApi) {
    this.employeeRepository = employeeRepository;
    this.userRepository = userRepository;
    this.adminApi = adminApi;
    this.departmentApi = departmentApi;
    this.leaveApi = leaveApi;
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

  @Override
  @Transactional(readOnly = true)
  public Map<Long, EmployeeSummaryResponse> findSummariesByIds(Collection<Long> employeeIds) {
    return employeeRepository.findAllById(employeeIds).stream()
        .collect(Collectors.toMap(Employee::getId, this::toEmployeeSummaryResponse));
  }

  @Override
  @Transactional(readOnly = true)
  public EmployeeSummaryResponse requireAdminAccessToEmployee(Long adminId, Long employeeId) {
    EmployeeSummaryResponse employee =
        findSummaryById(employeeId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    if (!adminApi.overseesDepartment(adminId, employee.getDepartmentId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to this employee");
    }
    return employee;
  }

  @Override
  @Transactional(readOnly = true)
  public Long getWeekScheduleId(Long employeeId) {
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"))
        .getWeekScheduleId();
  }

  @Override
  @Transactional
  public void assignWeekSchedule(Long employeeId, Long weekScheduleId) {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    employee.setWeekScheduleId(weekScheduleId);
    employeeRepository.save(employee);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> findActiveEmployeeIds() {
    return employeeRepository.findByActiveTrue().stream().map(Employee::getId).toList();
  }

  @Transactional(readOnly = true)
  public EmployeeListResponse getEmployeesForAdmin(Long id) {
    List<Long> deptIds = adminApi.departmentIdsForAdminUser(id);
    return toListResponse(employeeRepository.findByDepartmentIdIn(deptIds));
  }

  private EmployeeListResponse toListResponse(List<Employee> employees) {
    List<Long> employeeIds = employees.stream().map(Employee::getId).toList();
    Map<Long, ActiveLeaveResponse> activeLeaves =
        leaveApi.findActiveLeavesByEmployeeIds(employeeIds);
    List<EmployeeListItemResponse> items =
        employees.stream()
            .map(employee -> toEmployeeListItemResponse(employee, activeLeaves))
            .toList();
    return new EmployeeListResponse(items);
  }

  private EmployeeListItemResponse toEmployeeListItemResponse(
      Employee employee, Map<Long, ActiveLeaveResponse> activeLeaves) {
    User user =
        userRepository
            .findById(employee.getId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));
    String deptName = departmentApi.findNameById(employee.getDepartmentId()).orElse(null);

    return new EmployeeListItemResponse(
        employee.getId(),
        user.getName(),
        user.getSurname(),
        employee.getDepartmentId(),
        deptName,
        employee.isActive(),
        activeLeaves.get(employee.getId()));
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
