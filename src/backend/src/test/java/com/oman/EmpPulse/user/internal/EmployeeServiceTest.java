package com.oman.EmpPulse.user.internal;

import static com.oman.EmpPulse.support.Fixtures.employee;
import static com.oman.EmpPulse.support.Fixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.leave.api.ActiveLeaveResponse;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.leave.api.LeaveType;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.Employee;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import com.oman.EmpPulse.user.dto.EmployeeListResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

  @Mock private EmployeeRepository employeeRepository;
  @Mock private UserRepository userRepository;
  @Mock private AdminApi adminApi;
  @Mock private DepartmentApi departmentApi;
  @Mock private LeaveApi leaveApi;

  @InjectMocks private EmployeeService employeeService;

  private Employee employee;

  @BeforeEach
  void setUp() {
    employee = employee(10L, 1L, 20, true);
  }

  @Test
  void requireAdminAccessToEmployeeRejectsWhenAdminDoesNotOverseeDepartment() {
    when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
    when(userRepository.findById(10L))
        .thenReturn(Optional.of(user(10L, "Emp", "A", "e@x.com", "h", false, true)));
    when(departmentApi.findNameById(1L)).thenReturn(Optional.of("Dept"));
    when(adminApi.overseesDepartment(99L, 1L)).thenReturn(false);

    assertStatus(
        HttpStatus.FORBIDDEN,
        () -> employeeService.requireAdminAccessToEmployee(99L, 10L),
        "No access to this employee");
  }

  @Test
  void requireAdminAccessToEmployeeReturnsSummaryWhenAllowed() {
    when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
    when(userRepository.findById(10L))
        .thenReturn(Optional.of(user(10L, "Emp", "A", "e@x.com", "h", false, true)));
    when(departmentApi.findNameById(1L)).thenReturn(Optional.of("Dept"));
    when(adminApi.overseesDepartment(99L, 1L)).thenReturn(true);

    EmployeeSummaryResponse summary = employeeService.requireAdminAccessToEmployee(99L, 10L);

    assertThat(summary.getId()).isEqualTo(10L);
    assertThat(summary.getDepartmentName()).isEqualTo("Dept");
  }

  @Test
  void getEmployeesForAdminIncludesActiveLeaveInformation() {
    when(adminApi.departmentIdsForAdminUser(99L)).thenReturn(List.of(1L));
    when(employeeRepository.findByDepartmentIdIn(List.of(1L))).thenReturn(List.of(employee));
    when(userRepository.findById(10L))
        .thenReturn(Optional.of(user(10L, "Emp", "A", "e@x.com", "h", false, true)));
    when(departmentApi.findNameById(1L)).thenReturn(Optional.of("Dept"));
    when(leaveApi.findActiveLeavesByEmployeeIds(List.of(10L)))
        .thenReturn(
            Map.of(
                10L,
                new ActiveLeaveResponse(
                    LeaveType.vacation, LocalDate.now(), LocalDate.now().plusDays(2))));

    EmployeeListResponse response = employeeService.getEmployeesForAdmin(99L);

    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().getFirst().getActiveLeave()).isNotNull();
  }

  private void assertStatus(HttpStatus status, Runnable action, String messageContains) {
    assertThatThrownBy(action::run)
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            throwable -> {
              ResponseStatusException ex = (ResponseStatusException) throwable;
              assertThat(ex.getStatusCode()).isEqualTo(status);
              assertThat(ex.getReason()).contains(messageContains);
            });
  }
}
