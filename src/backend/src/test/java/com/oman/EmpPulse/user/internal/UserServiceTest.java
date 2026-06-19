package com.oman.EmpPulse.user.internal;

import static com.oman.EmpPulse.support.Fixtures.admin;
import static com.oman.EmpPulse.support.Fixtures.department;
import static com.oman.EmpPulse.support.Fixtures.employee;
import static com.oman.EmpPulse.support.Fixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.defaulthours.api.DefaultHoursApi;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.user.api.Employee;
import com.oman.EmpPulse.user.api.User;
import com.oman.EmpPulse.user.dto.BonusVacationDayRequest;
import com.oman.EmpPulse.user.dto.PasswordChangeRequest;
import com.oman.EmpPulse.user.dto.PreferencesUpdateRequest;
import com.oman.EmpPulse.user.dto.UserCreateRequest;
import com.oman.EmpPulse.user.dto.UserUpdateRequest;
import java.util.HashMap;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private AdminRepository adminRepository;
  @Mock private EmployeeRepository employeeRepository;
  @Mock private BonusVacationDaysRepository bonusVacationDaysRepository;
  @Mock private FindByIndexNameSessionRepository<Session> sessionRepository;
  @Mock private DepartmentApi departmentApi;
  @Mock private DefaultHoursApi defaultHoursApi;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private LeaveApi leaveApi;
  @Mock private NotificationApi notificationApi;

  @InjectMocks private UserService userService;

  private User activeUser;

  @BeforeEach
  void setUp() {
    activeUser = user(10L, "A", "User", "a@x.com", "hash", false, true);
  }

  @Test
  void createUserRejectsRolelessRequest() {
    UserCreateRequest req = baseCreateRequest();
    req.setEmployeeDepartmentId(null);

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> userService.createUser(req, 99L, true),
        "Cannot create an account with no role");
  }

  @Test
  void createUserRejectsAdminTryingToCreateAdmin() {
    UserCreateRequest req = baseCreateRequest();
    req.setAdminDepartmentIds(List.of(1L));

    assertStatus(
        HttpStatus.FORBIDDEN, () -> userService.createUser(req, 99L, false), "employee accounts");
  }

  @Test
  void createUserCreatesEmployeeAndSendsCredentials() {
    UserCreateRequest req = baseCreateRequest();
    when(departmentApi.findNameById(1L)).thenReturn(Optional.of("Dept"));
    when(adminRepository.findById(99L))
        .thenReturn(Optional.of(admin(99L, department(1L, "Dept", false))));
    when(userRepository.findByEmailAndActiveTrue("new@x.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("password")).thenReturn("encoded");
    when(defaultHoursApi.inheritDepartmentSchedule(1L)).thenReturn(123L);
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User saved = invocation.getArgument(0);
              saved.setId(77L);
              return saved;
            });

    Long id = userService.createUser(req, 99L, false);

    assertThat(id).isEqualTo(77L);
    verify(employeeRepository).save(any(Employee.class));
    verify(notificationApi)
        .sendAccountCredentials(any(), org.mockito.ArgumentMatchers.eq("password"));
  }

  @Test
  void changeMyPasswordRejectsMismatchedPasswordConfirmation() {
    PasswordChangeRequest req = new PasswordChangeRequest();
    req.setCurrentPassword("old");
    req.setNewPassword("new");
    req.setConfirmNewPassword("different");
    when(userRepository.findById(10L)).thenReturn(Optional.of(activeUser));
    when(passwordEncoder.matches("old", "hash")).thenReturn(true);

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> userService.changeMyPassword(10L, req, "s1"),
        "Passwords do not match");
  }

  @Test
  void changeMyPasswordInvalidatesOtherSessionsAndSendsNotification() {
    PasswordChangeRequest req = new PasswordChangeRequest();
    req.setCurrentPassword("old");
    req.setNewPassword("new");
    req.setConfirmNewPassword("new");
    when(userRepository.findById(10L)).thenReturn(Optional.of(activeUser));
    when(passwordEncoder.matches("old", "hash")).thenReturn(true);
    when(passwordEncoder.matches("new", "hash")).thenReturn(false);
    when(passwordEncoder.encode("new")).thenReturn("new-hash");
    Map<String, Session> sessions = new HashMap<>();
    sessions.put("s1", null);
    sessions.put("s2", null);
    when(sessionRepository.findByPrincipalName("10")).thenReturn(sessions);

    userService.changeMyPassword(10L, req, "s1");

    verify(sessionRepository).deleteById("s2");
    verify(sessionRepository, never()).deleteById("s1");
    verify(notificationApi).sendPasswordChangedNotification(any());
  }

  @Test
  void updatePreferencesRejectsInvalidTheme() {
    PreferencesUpdateRequest req = new PreferencesUpdateRequest();
    req.setTheme("invalid_theme");
    when(userRepository.findById(10L)).thenReturn(Optional.of(activeUser));

    assertStatus(
        HttpStatus.BAD_REQUEST, () -> userService.updatePreferences(10L, req), "Invalid theme");
  }

  @Test
  void updateUserRejectsAdminEditingOwnerOnlyFields() {
    UserUpdateRequest req = new UserUpdateRequest();
    req.setEmail("new@x.com");

    assertStatus(
        HttpStatus.FORBIDDEN,
        () -> userService.updateUser(10L, req, 99L, false),
        "Admins can only");
  }

  @Test
  void updateBonusVacationDaysRejectsNegativeDays() {
    BonusVacationDayRequest req = new BonusVacationDayRequest();
    req.setYear(2026);
    req.setDays(-1);
    when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee(10L, 1L, 20, true)));
    when(adminRepository.findById(99L))
        .thenReturn(Optional.of(admin(99L, department(1L, "Dept", false))));

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> userService.updateBonusVacationDays(10L, req, 99L),
        "greater or equal to 0");
  }

  private UserCreateRequest baseCreateRequest() {
    UserCreateRequest req = new UserCreateRequest();
    req.setName("New");
    req.setSurname("User");
    req.setEmail("new@x.com");
    req.setPassword("password");
    req.setEmployeeDepartmentId(1L);
    req.setYearlyVacationBalance(20);
    return req;
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
