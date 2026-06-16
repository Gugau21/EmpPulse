package com.oman.EmpPulse.integration;

import static com.oman.EmpPulse.support.Fixtures.leave;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.leave.dto.LeaveResponse;
import com.oman.EmpPulse.leave.dto.LeaveResponseRequest;
import com.oman.EmpPulse.leave.internal.Leave;
import com.oman.EmpPulse.leave.internal.LeaveRepository;
import com.oman.EmpPulse.leave.internal.LeaveService;
import com.oman.EmpPulse.leave.internal.LeaveStatus;
import com.oman.EmpPulse.leave.internal.LeaveType;
import com.oman.EmpPulse.loggedhours.internal.LoggedHours;
import com.oman.EmpPulse.loggedhours.internal.LoggedHoursRepository;
import com.oman.EmpPulse.loggedhours.internal.LoggedHoursService;
import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.UserContact;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Integration test: verifies that when an admin approves an unpaid leave request, the LeaveService
 * correctly triggers LoggedHoursService to clear the employee's logged hours for that date range.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Integration: Leave approval clears logged hours for unpaid leave")
class LeaveApprovalClearsLoggedHoursIT {

  @Mock private LeaveRepository leaveRepository;
  @Mock private LoggedHoursRepository loggedHoursRepository;
  @Mock private EmployeeApi employeeApi;
  @Mock private AdminApi adminApi;
  @Mock private UserApi userApi;
  @Mock private NotificationApi notificationApi;

  private LoggedHoursService loggedHoursService;
  private LeaveService leaveService;

  @BeforeEach
  void setUp() {
    loggedHoursService = new LoggedHoursService(loggedHoursRepository, employeeApi);
    leaveService =
        new LeaveService(
            leaveRepository, employeeApi, adminApi, loggedHoursService, userApi, notificationApi);
  }

  @Test
  void adminApprovingUnpaidLeaveDeletesLoggedHoursInRange() {
    Long employeeId = 10L;
    Long adminId = 99L;
    LocalDate start = LocalDate.of(2026, 7, 1);
    LocalDate end = LocalDate.of(2026, 7, 3);

    Leave pendingLeave =
        leave(
            50L, employeeId, LeaveType.personal, LeaveStatus.pending, false, start, end, "family");
    EmployeeSummaryResponse employee =
        new EmployeeSummaryResponse(employeeId, "Emp", "One", 200L, "Engineering", true);

    when(leaveRepository.findById(50L)).thenReturn(Optional.of(pendingLeave));
    when(employeeApi.findSummaryById(employeeId)).thenReturn(Optional.of(employee));
    when(adminApi.overseesDepartment(adminId, 200L)).thenReturn(true);
    when(leaveRepository.saveAndFlush(any(Leave.class))).thenAnswer(inv -> inv.getArgument(0));
    when(userApi.findContactById(employeeId))
        .thenReturn(Optional.of(new UserContact("emp@x.com", "Emp")));

    LoggedHours existing =
        new LoggedHours(employeeId, adminId, start, LocalTime.of(9, 0), LocalTime.of(17, 0));
    ReflectionTestUtils.setField(existing, "id", 1L);
    when(loggedHoursRepository.findAllByEmployeeIdAndDateBetween(employeeId, start, end))
        .thenReturn(List.of(existing));

    LeaveResponseRequest req = new LeaveResponseRequest();
    req.setStatus(LeaveStatus.approved);

    LeaveResponse response = leaveService.respondToLeaveRequest(50L, req, adminId);

    assertThat(response.getStatus()).isEqualTo(LeaveStatus.approved);
    verify(loggedHoursRepository).deleteAll(List.of(existing));
  }

  @Test
  void adminApprovingPaidLeaveDoesNotClearLoggedHours() {
    Long employeeId = 10L;
    Long adminId = 99L;
    LocalDate start = LocalDate.of(2026, 7, 1);
    LocalDate end = LocalDate.of(2026, 7, 3);

    Leave pendingLeave =
        leave(51L, employeeId, LeaveType.vacation, LeaveStatus.pending, true, start, end, null);
    EmployeeSummaryResponse employee =
        new EmployeeSummaryResponse(employeeId, "Emp", "One", 200L, "Engineering", true);

    when(leaveRepository.findById(51L)).thenReturn(Optional.of(pendingLeave));
    when(employeeApi.findSummaryById(employeeId)).thenReturn(Optional.of(employee));
    when(adminApi.overseesDepartment(adminId, 200L)).thenReturn(true);
    when(leaveRepository.saveAndFlush(any(Leave.class))).thenAnswer(inv -> inv.getArgument(0));
    when(userApi.findContactById(employeeId))
        .thenReturn(Optional.of(new UserContact("emp@x.com", "Emp")));

    LeaveResponseRequest req = new LeaveResponseRequest();
    req.setStatus(LeaveStatus.approved);

    LeaveResponse response = leaveService.respondToLeaveRequest(51L, req, adminId);

    assertThat(response.getStatus()).isEqualTo(LeaveStatus.approved);
    verify(loggedHoursRepository, org.mockito.Mockito.never())
        .findAllByEmployeeIdAndDateBetween(any(), any(), any());
  }
}
