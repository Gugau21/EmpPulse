package com.oman.EmpPulse.loggedhours.internal;

import static com.oman.EmpPulse.support.Fixtures.admin;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.defaulthours.api.DefaultDayIntervalResponse;
import com.oman.EmpPulse.defaulthours.api.DefaultHoursApi;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoLogSchedulerTest {

  @Mock private EmployeeApi employeeApi;
  @Mock private DefaultHoursApi defaultHoursApi;
  @Mock private LeaveApi leaveApi;
  @Mock private AdminApi adminApi;
  @Mock private LoggedHoursService loggedHoursService;

  private AutoLogScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler =
        new AutoLogScheduler(
            employeeApi, defaultHoursApi, leaveApi, adminApi, loggedHoursService, "UTC");
  }

  @Test
  void autoLogForTodaySkipsWhenOwnerAdminIsMissing() {
    when(adminApi.getOwnerAdmin()).thenReturn(Optional.empty());

    scheduler.autoLogForToday();

    verify(employeeApi, never()).findActiveEmployeeIds();
  }

  @Test
  void autoLogForTodaySkipsUnpaidLeaveEmployees() {
    when(adminApi.getOwnerAdmin()).thenReturn(Optional.of(admin(1L)));
    when(employeeApi.findActiveEmployeeIds()).thenReturn(List.of(10L, 20L));
    when(leaveApi.findEmployeeIdsOnUnpaidApprovedLeave(
            org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(Set.of(10L));
    when(defaultHoursApi.findEmployeeIntervalForDay(
            org.mockito.ArgumentMatchers.eq(20L), org.mockito.ArgumentMatchers.anyInt()))
        .thenReturn(
            Optional.of(new DefaultDayIntervalResponse(LocalTime.of(9, 0), LocalTime.of(17, 0))));

    scheduler.autoLogForToday();

    verify(loggedHoursService, never())
        .createAutoLogIfAbsent(
            org.mockito.ArgumentMatchers.eq(10L),
            anyLong(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
    verify(loggedHoursService)
        .createAutoLogIfAbsent(
            org.mockito.ArgumentMatchers.eq(20L),
            anyLong(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.eq(LocalTime.of(9, 0)),
            org.mockito.ArgumentMatchers.eq(LocalTime.of(17, 0)));
  }
}
