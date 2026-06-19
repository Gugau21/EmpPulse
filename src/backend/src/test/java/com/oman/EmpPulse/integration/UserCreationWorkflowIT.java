package com.oman.EmpPulse.integration;

import static com.oman.EmpPulse.support.Fixtures.admin;
import static com.oman.EmpPulse.support.Fixtures.department;
import static com.oman.EmpPulse.support.Fixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.defaulthours.api.DefaultHoursApi;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.notification.api.NotificationRecipient;
import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.api.Employee;
import com.oman.EmpPulse.user.api.User;
import com.oman.EmpPulse.user.dto.UserCreateRequest;
import com.oman.EmpPulse.user.internal.AdminRepository;
import com.oman.EmpPulse.user.internal.BonusVacationDaysRepository;
import com.oman.EmpPulse.user.internal.EmployeeRepository;
import com.oman.EmpPulse.user.internal.UserRepository;
import com.oman.EmpPulse.user.internal.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.web.server.ResponseStatusException;

/**
 * Integration test: verifies that creating a new employee user triggers the full workflow:
 * department validation → employee record creation with inherited schedule → credential
 * notification. UserService is real; repositories and external APIs are mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Integration: User creation triggers schedule inheritance + notification")
class UserCreationWorkflowIT {

  @Mock private UserRepository userRepository;
  @Mock private AdminRepository adminRepository;
  @Mock private EmployeeRepository employeeRepository;
  @Mock private BonusVacationDaysRepository bonusVacationDaysRepository;
  @Mock private FindByIndexNameSessionRepository<? extends Session> sessionRepository;
  @Mock private DepartmentApi departmentApi;
  @Mock private DefaultHoursApi defaultHoursApi;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private LeaveApi leaveApi;
  @Mock private NotificationApi notificationApi;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService =
        new UserService(
            userRepository,
            adminRepository,
            employeeRepository,
            bonusVacationDaysRepository,
            sessionRepository,
            departmentApi,
            defaultHoursApi,
            passwordEncoder,
            leaveApi,
            notificationApi);
  }

  @Test
  void createEmployeeUser_inheritsScheduleAndSendsCredentialEmail() {
    Long callerAdminId = 1L;
    Long departmentId = 100L;
    Admin caller = admin(callerAdminId, department(departmentId, "Engineering", false));

    when(adminRepository.findById(callerAdminId)).thenReturn(Optional.of(caller));
    when(departmentApi.findNameById(departmentId)).thenReturn(Optional.of("Engineering"));
    when(userRepository.findByEmailAndActiveTrue("new@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("secret123")).thenReturn("$encoded$");
    when(defaultHoursApi.inheritDepartmentSchedule(departmentId)).thenReturn(42L);

    when(userRepository.save(any(User.class)))
        .thenAnswer(
            inv -> {
              User u = inv.getArgument(0);
              u.setId(50L);
              return u;
            });

    UserCreateRequest req = new UserCreateRequest();
    req.setName("New");
    req.setSurname("User");
    req.setEmail("new@example.com");
    req.setPassword("secret123");
    req.setEmployeeDepartmentId(departmentId);
    req.setYearlyVacationBalance(20);

    Long createdUserId = userService.createUser(req, callerAdminId, false);

    assertThat(createdUserId).isEqualTo(50L);

    verify(defaultHoursApi).inheritDepartmentSchedule(departmentId);

    ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
    verify(employeeRepository).save(empCaptor.capture());
    Employee savedEmployee = empCaptor.getValue();
    assertThat(savedEmployee.getDepartmentId()).isEqualTo(departmentId);
    assertThat(savedEmployee.getVacationBalance()).isEqualTo(20);

    ArgumentCaptor<NotificationRecipient> recipientCaptor =
        ArgumentCaptor.forClass(NotificationRecipient.class);
    verify(notificationApi).sendAccountCredentials(recipientCaptor.capture(), eq("secret123"));
    assertThat(recipientCaptor.getValue().email()).isEqualTo("new@example.com");
    assertThat(recipientCaptor.getValue().name()).isEqualTo("New");
  }

  @Test
  void createAdminUser_onlyOwnerCanDoIt_regularAdminGetsForbidden() {
    Long callerAdminId = 1L;

    UserCreateRequest req = new UserCreateRequest();
    req.setName("Admin");
    req.setSurname("New");
    req.setEmail("admin@example.com");
    req.setPassword("pass123");
    req.setAdminDepartmentIds(List.of(10L, 20L));

    assertThatThrownBy(() -> userService.createUser(req, callerAdminId, false))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Admins can only create employee accounts");

    verify(notificationApi, never()).sendAccountCredentials(any(), any());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void createUserWithDuplicateEmail_conflictErrorAndNoNotification() {
    Long callerAdminId = 1L;
    Long departmentId = 100L;
    Admin caller = admin(callerAdminId, department(departmentId, "Engineering", false));

    when(adminRepository.findById(callerAdminId)).thenReturn(Optional.of(caller));
    when(departmentApi.findNameById(departmentId)).thenReturn(Optional.of("Engineering"));
    when(userRepository.findByEmailAndActiveTrue("taken@example.com"))
        .thenReturn(
            Optional.of(user(99L, "Existing", "User", "taken@example.com", "hash", false, true)));

    UserCreateRequest req = new UserCreateRequest();
    req.setName("Dup");
    req.setSurname("User");
    req.setEmail("taken@example.com");
    req.setPassword("pass");
    req.setEmployeeDepartmentId(departmentId);
    req.setYearlyVacationBalance(10);

    assertThatThrownBy(() -> userService.createUser(req, callerAdminId, false))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Email already in use");

    verify(notificationApi, never()).sendAccountCredentials(any(), any());
  }
}
