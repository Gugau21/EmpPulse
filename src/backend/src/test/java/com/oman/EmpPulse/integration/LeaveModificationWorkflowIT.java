package com.oman.EmpPulse.integration;

import static com.oman.EmpPulse.support.Fixtures.leave;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.oman.EmpPulse.leave.api.Leave;
import com.oman.EmpPulse.leave.api.LeaveStatus;
import com.oman.EmpPulse.leave.api.LeaveType;
import com.oman.EmpPulse.leave.dto.LeaveModificationRequest;
import com.oman.EmpPulse.leave.dto.LeaveResponse;
import com.oman.EmpPulse.leave.dto.LeaveResponseRequest;
import com.oman.EmpPulse.leave.internal.LeaveRepository;
import com.oman.EmpPulse.leave.internal.LeaveService;
import com.oman.EmpPulse.loggedhours.api.LoggedHoursApi;
import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.UserContact;
import java.time.LocalDate;
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
 * Integration test: verifies the full leave modification workflow where an employee proposes
 * changes to an approved request, then an admin approves/rejects the modification. Tests the
 * interaction between createModification and respondToLeaveRequest (resolveModification path).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Integration: Leave modification proposal + admin resolution workflow")
class LeaveModificationWorkflowIT {

  @Mock private LeaveRepository leaveRepository;
  @Mock private EmployeeApi employeeApi;
  @Mock private AdminApi adminApi;
  @Mock private LoggedHoursApi loggedHoursApi;
  @Mock private UserApi userApi;
  @Mock private NotificationApi notificationApi;

  private LeaveService leaveService;

  private static final Long EMPLOYEE_ID = 10L;
  private static final Long ADMIN_ID = 99L;
  private static final Long DEPT_ID = 200L;

  private EmployeeSummaryResponse employee;

  @BeforeEach
  void setUp() {
    leaveService =
        new LeaveService(
            leaveRepository, employeeApi, adminApi, loggedHoursApi, userApi, notificationApi);
    employee = new EmployeeSummaryResponse(EMPLOYEE_ID, "Emp", "One", DEPT_ID, "Engineering", true);
  }

  @Test
  void employeeProposesModificationThenAdminApproves_originalIsUpdatedModificationDeleted() {
    Leave original =
        leave(
            100L,
            EMPLOYEE_ID,
            LeaveType.vacation,
            LeaveStatus.approved,
            true,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 5),
            null);

    when(leaveRepository.findById(100L)).thenReturn(Optional.of(original));
    when(leaveRepository.existsByModificationId(100L)).thenReturn(false);
    when(leaveRepository.existsActiveOverlapExcludingIds(
            EMPLOYEE_ID, LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 5), List.of(100L)))
        .thenReturn(false);
    when(leaveRepository.save(any(Leave.class)))
        .thenAnswer(
            inv -> {
              Leave saved = inv.getArgument(0);
              ReflectionTestUtils.setField(saved, "id", 101L);
              ReflectionTestUtils.setField(saved, "createdAt", java.time.OffsetDateTime.now());
              ReflectionTestUtils.setField(saved, "updatedAt", java.time.OffsetDateTime.now());
              return saved;
            });
    when(employeeApi.findSummaryById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

    LeaveModificationRequest modReq = new LeaveModificationRequest();
    modReq.setStartDate(LocalDate.of(2026, 7, 2));

    LeaveResponse modResponse = leaveService.createModification(100L, modReq, EMPLOYEE_ID);

    assertThat(modResponse.getStatus()).isEqualTo(LeaveStatus.pending);
    assertThat(modResponse.getStartDate()).isEqualTo(LocalDate.of(2026, 7, 2));

    // Now simulate admin approving the modification
    Leave modificationLeave =
        leave(
            101L,
            EMPLOYEE_ID,
            LeaveType.vacation,
            LeaveStatus.pending,
            true,
            LocalDate.of(2026, 7, 2),
            LocalDate.of(2026, 7, 5),
            null);
    ReflectionTestUtils.setField(modificationLeave, "modificationId", 100L);

    when(leaveRepository.findById(101L)).thenReturn(Optional.of(modificationLeave));
    when(leaveRepository.findById(100L)).thenReturn(Optional.of(original));
    when(adminApi.overseesDepartment(ADMIN_ID, DEPT_ID)).thenReturn(true);
    when(leaveRepository.saveAndFlush(any(Leave.class))).thenAnswer(inv -> inv.getArgument(0));
    when(userApi.findContactById(EMPLOYEE_ID))
        .thenReturn(Optional.of(new UserContact("emp@x.com", "Emp")));

    LeaveResponseRequest approveReq = new LeaveResponseRequest();
    approveReq.setStatus(LeaveStatus.approved);

    LeaveResponse approvalResponse = leaveService.respondToLeaveRequest(101L, approveReq, ADMIN_ID);

    assertThat(approvalResponse.getStatus()).isEqualTo(LeaveStatus.approved);
    assertThat(approvalResponse.getStartDate()).isEqualTo(LocalDate.of(2026, 7, 2));
    verify(leaveRepository).delete(modificationLeave);
  }

  @Test
  void employeeProposesModificationThenAdminRejects_modificationBecomesStandaloneRejected() {
    Leave original =
        leave(
            100L,
            EMPLOYEE_ID,
            LeaveType.sick,
            LeaveStatus.approved,
            true,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 3),
            null);

    Leave modificationLeave =
        leave(
            102L,
            EMPLOYEE_ID,
            LeaveType.sick,
            LeaveStatus.pending,
            true,
            LocalDate.of(2026, 8, 2),
            LocalDate.of(2026, 8, 4),
            null);
    ReflectionTestUtils.setField(modificationLeave, "modificationId", 100L);

    when(leaveRepository.findById(102L)).thenReturn(Optional.of(modificationLeave));
    when(employeeApi.findSummaryById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
    when(adminApi.overseesDepartment(ADMIN_ID, DEPT_ID)).thenReturn(true);
    when(leaveRepository.saveAndFlush(any(Leave.class))).thenAnswer(inv -> inv.getArgument(0));
    when(userApi.findContactById(EMPLOYEE_ID))
        .thenReturn(Optional.of(new UserContact("emp@x.com", "Emp")));

    LeaveResponseRequest rejectReq = new LeaveResponseRequest();
    rejectReq.setStatus(LeaveStatus.rejected);

    LeaveResponse response = leaveService.respondToLeaveRequest(102L, rejectReq, ADMIN_ID);

    assertThat(response.getStatus()).isEqualTo(LeaveStatus.rejected);
    assertThat(modificationLeave.getModificationId()).isNull();
    verify(notificationApi).sendLeaveDecision(any(), any(), org.mockito.ArgumentMatchers.eq(false));
  }
}
