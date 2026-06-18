package com.oman.EmpPulse.leave.internal;

import static com.oman.EmpPulse.support.Fixtures.leave;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.oman.EmpPulse.leave.api.Leave;
import com.oman.EmpPulse.leave.api.LeaveStatus;
import com.oman.EmpPulse.leave.api.LeaveType;
import com.oman.EmpPulse.leave.dto.LeaveCreateRequest;
import com.oman.EmpPulse.leave.dto.LeaveModificationRequest;
import com.oman.EmpPulse.leave.dto.LeaveResponse;
import com.oman.EmpPulse.leave.dto.LeaveResponseRequest;
import com.oman.EmpPulse.leave.dto.LeaveUpdateRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

  @Mock private LeaveRepository leaveRepository;
  @Mock private EmployeeApi employeeApi;
  @Mock private AdminApi adminApi;
  @Mock private LoggedHoursApi loggedHoursApi;
  @Mock private UserApi userApi;
  @Mock private NotificationApi notificationApi;

  @InjectMocks private LeaveService leaveService;

  private EmployeeSummaryResponse employee;

  @BeforeEach
  void setUp() {
    employee = new EmployeeSummaryResponse(10L, "Emp", "One", 200L, "Engineering", true);
  }

  @Test
  void createLeaveRequestRejectsMissingRequiredFields() {
    LeaveCreateRequest req = new LeaveCreateRequest();

    assertStatus(
        HttpStatus.BAD_REQUEST, () -> leaveService.createLeaveRequest(req, 10L, false), "required");
  }

  @Test
  void createLeaveRequestRejectsPersonalLeaveWithoutReason() {
    LeaveCreateRequest req = validCreateRequest();
    req.setType(LeaveType.personal);
    req.setReason(" ");
    when(employeeApi.findSummaryById(10L)).thenReturn(Optional.of(employee));

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> leaveService.createLeaveRequest(req, 10L, false),
        "Reason is required");
  }

  @Test
  void createLeaveRequestRejectsOverlappingLeave() {
    LeaveCreateRequest req = validCreateRequest();
    when(employeeApi.findSummaryById(10L)).thenReturn(Optional.of(employee));
    when(leaveRepository.existsActiveOverlap(10L, req.getStartDate(), req.getEndDate()))
        .thenReturn(true);

    assertStatus(
        HttpStatus.CONFLICT,
        () -> leaveService.createLeaveRequest(req, 10L, false),
        "Overlapping leave request exists");
  }

  @Test
  void createLeaveRequestAdminOnBehalfAutoApprovesAndSendsNotification() {
    LeaveCreateRequest req = validCreateRequest();
    req.setEmployeeId(10L);
    when(employeeApi.findSummaryById(10L)).thenReturn(Optional.of(employee));
    when(leaveRepository.existsActiveOverlap(10L, req.getStartDate(), req.getEndDate()))
        .thenReturn(false);
    when(adminApi.overseesDepartment(99L, 200L)).thenReturn(true);
    when(leaveRepository.save(any(Leave.class)))
        .thenAnswer(
            invocation -> {
              Leave saved = invocation.getArgument(0);
              return leave(
                  500L,
                  saved.getEmployeeId(),
                  saved.getType(),
                  saved.getStatus(),
                  saved.isPaid(),
                  saved.getStartDate(),
                  saved.getEndDate(),
                  saved.getDescription());
            });
    when(userApi.findContactById(10L)).thenReturn(Optional.of(new UserContact("emp@x.com", "Emp")));

    LeaveResponse response = leaveService.createLeaveRequest(req, 99L, true);

    assertThat(response.getStatus()).isEqualTo(LeaveStatus.approved);
    verify(notificationApi).sendLeaveCreatedOnBehalf(any(), any());
  }

  @Test
  void createLeaveRequestForbidsOnBehalfWithoutOversight() {
    LeaveCreateRequest req = validCreateRequest();
    when(employeeApi.findSummaryById(10L)).thenReturn(Optional.of(employee));
    when(leaveRepository.existsActiveOverlap(10L, req.getStartDate(), req.getEndDate()))
        .thenReturn(false);
    when(adminApi.overseesDepartment(99L, 200L)).thenReturn(false);

    assertStatus(
        HttpStatus.FORBIDDEN,
        () -> leaveService.createLeaveRequest(req, 99L, true),
        "No access to this employee");
  }

  @Test
  void updateLeaveRequestRejectsEmployeeEditingOwnApprovedRequest() {
    Leave original =
        leave(
            11L,
            10L,
            LeaveType.vacation,
            LeaveStatus.approved,
            true,
            day(2026, 6, 1),
            day(2026, 6, 3),
            null);
    when(leaveRepository.findById(11L)).thenReturn(Optional.of(original));
    when(employeeApi.findSummaryById(10L)).thenReturn(Optional.of(employee));
    when(adminApi.overseesDepartment(10L, 200L)).thenReturn(false);

    assertStatus(
        HttpStatus.CONFLICT,
        () -> leaveService.updateLeaveRequest(11L, new LeaveUpdateRequest(), 10L, false),
        "modification request");
  }

  @Test
  void updateLeaveRequestByAdminApprovesAndNotifiesEmployee() {
    Leave original =
        leave(
            11L,
            10L,
            LeaveType.vacation,
            LeaveStatus.pending,
            true,
            day(2026, 6, 1),
            day(2026, 6, 2),
            null);
    LeaveUpdateRequest req = new LeaveUpdateRequest();
    req.setEndDate(day(2026, 6, 4));
    when(leaveRepository.findById(11L)).thenReturn(Optional.of(original));
    when(employeeApi.findSummaryById(10L)).thenReturn(Optional.of(employee));
    when(adminApi.overseesDepartment(99L, 200L)).thenReturn(true);
    when(leaveRepository.existsByModificationId(11L)).thenReturn(false);
    when(leaveRepository.existsActiveOverlapExcludingIds(
            10L, day(2026, 6, 1), day(2026, 6, 4), List.of(11L)))
        .thenReturn(false);
    when(leaveRepository.saveAndFlush(any(Leave.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(userApi.findContactById(10L)).thenReturn(Optional.of(new UserContact("emp@x.com", "Emp")));

    LeaveResponse response = leaveService.updateLeaveRequest(11L, req, 99L, true);

    assertThat(response.getStatus()).isEqualTo(LeaveStatus.approved);
    verify(notificationApi).sendLeaveModifiedByAdmin(any(), any());
  }

  @Test
  void respondToLeaveRequestRejectsInvalidDecisionStatus() {
    Leave leave =
        leave(
            21L,
            10L,
            LeaveType.vacation,
            LeaveStatus.pending,
            true,
            day(2026, 6, 1),
            day(2026, 6, 2),
            null);
    LeaveResponseRequest req = new LeaveResponseRequest();
    req.setStatus(LeaveStatus.cancelled);
    when(leaveRepository.findById(21L)).thenReturn(Optional.of(leave));
    when(employeeApi.findSummaryById(10L)).thenReturn(Optional.of(employee));
    when(adminApi.overseesDepartment(99L, 200L)).thenReturn(true);

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> leaveService.respondToLeaveRequest(21L, req, 99L),
        "Decision must be approved or rejected");
  }

  @Test
  void respondToLeaveRequestApprovesAndClearsLoggedHoursWhenUnpaid() {
    Leave pending =
        leave(
            31L,
            10L,
            LeaveType.personal,
            LeaveStatus.pending,
            false,
            day(2026, 6, 10),
            day(2026, 6, 10),
            "x");
    LeaveResponseRequest req = new LeaveResponseRequest();
    req.setStatus(LeaveStatus.approved);
    when(leaveRepository.findById(31L)).thenReturn(Optional.of(pending));
    when(employeeApi.findSummaryById(10L)).thenReturn(Optional.of(employee));
    when(adminApi.overseesDepartment(99L, 200L)).thenReturn(true);
    when(leaveRepository.saveAndFlush(any(Leave.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(userApi.findContactById(10L)).thenReturn(Optional.of(new UserContact("emp@x.com", "Emp")));

    LeaveResponse response = leaveService.respondToLeaveRequest(31L, req, 99L);

    assertThat(response.getStatus()).isEqualTo(LeaveStatus.approved);
    verify(loggedHoursApi).deleteByEmployeeAndDateRange(10L, day(2026, 6, 10), day(2026, 6, 10));
    verify(notificationApi).sendLeaveDecision(any(), any(), org.mockito.ArgumentMatchers.eq(true));
  }

  @Test
  void cancelLeaveRequestRequiresOwnApprovedLeave() {
    Leave leave =
        leave(
            41L,
            10L,
            LeaveType.vacation,
            LeaveStatus.pending,
            true,
            day(2026, 6, 1),
            day(2026, 6, 2),
            null);
    when(leaveRepository.findById(41L)).thenReturn(Optional.of(leave));

    assertStatus(
        HttpStatus.CONFLICT,
        () -> leaveService.cancelLeaveRequest(41L, 10L),
        "Only approved leave requests can be cancelled");
  }

  @Test
  void createModificationRejectsUnchangedPayload() {
    Leave original =
        leave(
            51L,
            10L,
            LeaveType.vacation,
            LeaveStatus.approved,
            true,
            day(2026, 6, 1),
            day(2026, 6, 3),
            "note");
    LeaveModificationRequest req = new LeaveModificationRequest();
    when(leaveRepository.findById(51L)).thenReturn(Optional.of(original));
    when(leaveRepository.existsByModificationId(51L)).thenReturn(false);

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> leaveService.createModification(51L, req, 10L),
        "must change at least one field");
  }

  @Test
  void deleteLeaveRequestAllowsOnlyOwnerOfPendingRequest() {
    Leave leave =
        leave(
            61L,
            10L,
            LeaveType.sick,
            LeaveStatus.pending,
            true,
            day(2026, 6, 1),
            day(2026, 6, 2),
            null);
    when(leaveRepository.findById(61L)).thenReturn(Optional.of(leave));

    leaveService.deleteLeaveRequest(61L, 10L);

    verify(leaveRepository).delete(leave);
  }

  @Test
  void countUsedVacationDaysCountsOnlyWeekdaysWithinYear() {
    Leave first =
        leave(
            71L,
            10L,
            LeaveType.vacation,
            LeaveStatus.approved,
            true,
            day(2025, 12, 30),
            day(2026, 1, 2),
            null);
    Leave second =
        leave(
            72L,
            10L,
            LeaveType.vacation,
            LeaveStatus.pending,
            true,
            day(2026, 1, 5),
            day(2026, 1, 6),
            null);
    when(leaveRepository.findActiveVacationLeavesOverlapping(
            10L, day(2026, 1, 1), day(2026, 12, 31)))
        .thenReturn(List.of(first, second));

    int used = leaveService.countUsedVacationDays(10L, 2026);

    assertThat(used).isEqualTo(4);
  }

  @Test
  void findActiveLeavesThrowsOnDataInconsistency() {
    Leave leave1 =
        leave(
            81L,
            10L,
            LeaveType.sick,
            LeaveStatus.approved,
            true,
            day(2026, 6, 1),
            day(2026, 6, 2),
            null);
    Leave leave2 =
        leave(
            82L,
            10L,
            LeaveType.personal,
            LeaveStatus.approved,
            false,
            day(2026, 6, 1),
            day(2026, 6, 2),
            "x");
    when(leaveRepository.findActiveApprovedByEmployeeIds(List.of(10L), LocalDate.now()))
        .thenReturn(List.of(leave1, leave2));

    assertStatus(
        HttpStatus.INTERNAL_SERVER_ERROR,
        () -> leaveService.findActiveLeavesByEmployeeIds(List.of(10L)),
        "Data inconsistency");
  }

  @Test
  void findEmployeeIdsOnUnpaidApprovedLeaveReturnsEmptyForNullInput() {
    assertThat(leaveService.findEmployeeIdsOnUnpaidApprovedLeave(null, day(2026, 6, 1))).isEmpty();
    verify(leaveRepository, never()).findEmployeeIdsOnUnpaidApprovedLeave(any(), any());
  }

  private LeaveCreateRequest validCreateRequest() {
    LeaveCreateRequest req = new LeaveCreateRequest();
    req.setEmployeeId(10L);
    req.setType(LeaveType.vacation);
    req.setPaid(true);
    req.setStartDate(day(2026, 6, 1));
    req.setEndDate(day(2026, 6, 3));
    req.setReason("summer");
    return req;
  }

  private LocalDate day(int year, int month, int day) {
    return LocalDate.of(year, month, day);
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
