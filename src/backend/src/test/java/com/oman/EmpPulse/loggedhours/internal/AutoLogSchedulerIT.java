package com.oman.EmpPulse.loggedhours.internal;

import static com.oman.EmpPulse.support.Fixtures.admin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.defaulthours.api.DefaultDayIntervalResponse;
import com.oman.EmpPulse.defaulthours.api.DefaultHoursApi;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.loggedhours.api.LoggedHours;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Integration test: verifies the full auto-log workflow where AutoLogScheduler uses a real
 * LoggedHoursService instance. Tests the interaction between scheduler logic, default hours lookup,
 * leave checking, and actual logged-hours creation/merge behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Integration: AutoLogScheduler + LoggedHoursService workflow")
class AutoLogSchedulerIT {

  @Mock private LoggedHoursRepository loggedHoursRepository;
  @Mock private EmployeeApi employeeApi;
  @Mock private DefaultHoursApi defaultHoursApi;
  @Mock private LeaveApi leaveApi;
  @Mock private AdminApi adminApi;

  private LoggedHoursService loggedHoursService;
  private AutoLogScheduler scheduler;

  @BeforeEach
  void setUp() {
    loggedHoursService = new LoggedHoursService(loggedHoursRepository, employeeApi);
    scheduler =
        new AutoLogScheduler(
            employeeApi, defaultHoursApi, leaveApi, adminApi, loggedHoursService, "UTC");
  }

  @Test
  void autoLogCreatesEntryForEmployeeWithDefaultHoursAndNoExistingLog() {
    when(adminApi.getOwnerAdmin()).thenReturn(Optional.of(admin(1L)));
    when(employeeApi.findActiveEmployeeIds()).thenReturn(List.of(10L));
    when(leaveApi.findEmployeeIdsOnUnpaidApprovedLeave(any(), any())).thenReturn(Set.of());

    int todayDow = LocalDate.now().getDayOfWeek().getValue() - 1;
    when(defaultHoursApi.findEmployeeIntervalForDay(10L, todayDow))
        .thenReturn(
            Optional.of(new DefaultDayIntervalResponse(LocalTime.of(9, 0), LocalTime.of(17, 0))));
    when(loggedHoursRepository.findAllByEmployeeIdAndDate(eq(10L), any())).thenReturn(List.of());

    scheduler.autoLogForToday();

    ArgumentCaptor<LoggedHours> captor = ArgumentCaptor.forClass(LoggedHours.class);
    verify(loggedHoursRepository).save(captor.capture());
    LoggedHours saved = captor.getValue();
    assertThat(saved.getEmployeeId()).isEqualTo(10L);
    assertThat(saved.getAdminId()).isEqualTo(1L);
    assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(17, 0));
  }

  @Test
  void autoLogSkipsEmployeeOnUnpaidLeaveEvenIfDefaultHoursExist() {
    when(adminApi.getOwnerAdmin()).thenReturn(Optional.of(admin(1L)));
    when(employeeApi.findActiveEmployeeIds()).thenReturn(List.of(10L, 20L));
    when(leaveApi.findEmployeeIdsOnUnpaidApprovedLeave(any(), any())).thenReturn(Set.of(10L));

    int todayDow = LocalDate.now().getDayOfWeek().getValue() - 1;
    when(defaultHoursApi.findEmployeeIntervalForDay(20L, todayDow))
        .thenReturn(
            Optional.of(new DefaultDayIntervalResponse(LocalTime.of(8, 0), LocalTime.of(16, 0))));
    when(loggedHoursRepository.findAllByEmployeeIdAndDate(eq(20L), any())).thenReturn(List.of());

    scheduler.autoLogForToday();

    verify(defaultHoursApi, never()).findEmployeeIntervalForDay(eq(10L), any(Integer.class));
    ArgumentCaptor<LoggedHours> captor = ArgumentCaptor.forClass(LoggedHours.class);
    verify(loggedHoursRepository).save(captor.capture());
    assertThat(captor.getValue().getEmployeeId()).isEqualTo(20L);
    assertThat(captor.getValue().getStartTime()).isEqualTo(LocalTime.of(8, 0));
    assertThat(captor.getValue().getEndTime()).isEqualTo(LocalTime.of(16, 0));
  }

  @Test
  void autoLogSkipsEmployeeWhoAlreadyHasLoggedHoursForToday() {
    when(adminApi.getOwnerAdmin()).thenReturn(Optional.of(admin(1L)));
    when(employeeApi.findActiveEmployeeIds()).thenReturn(List.of(10L));
    when(leaveApi.findEmployeeIdsOnUnpaidApprovedLeave(any(), any())).thenReturn(Set.of());

    int todayDow = LocalDate.now().getDayOfWeek().getValue() - 1;
    when(defaultHoursApi.findEmployeeIntervalForDay(10L, todayDow))
        .thenReturn(
            Optional.of(new DefaultDayIntervalResponse(LocalTime.of(9, 0), LocalTime.of(17, 0))));
    when(loggedHoursRepository.findAllByEmployeeIdAndDate(eq(10L), any()))
        .thenReturn(
            List.of(
                new LoggedHours(
                    10L, 1L, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(17, 0))));

    scheduler.autoLogForToday();

    verify(loggedHoursRepository, never()).save(any(LoggedHours.class));
  }
}
