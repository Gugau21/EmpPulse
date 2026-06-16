package com.oman.EmpPulse.defaulthours.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.defaulthours.api.DefaultDayIntervalResponse;
import com.oman.EmpPulse.defaulthours.dto.DefaultWeekHoursDayRequest;
import com.oman.EmpPulse.defaulthours.dto.DefaultWeekHoursRequest;
import com.oman.EmpPulse.defaulthours.dto.DefaultWeekHoursResponse;
import com.oman.EmpPulse.defaulthours.dto.TimeIntervalRequest;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.EmployeeSummaryResponse;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DefaultHoursServiceTest {

  @Mock private WeekScheduleRepository weekScheduleRepository;
  @Mock private ScheduleBlockRepository scheduleBlockRepository;
  @Mock private EmployeeApi employeeApi;
  @Mock private DepartmentApi departmentApi;

  @InjectMocks private DefaultHoursService defaultHoursService;

  @Test
  void setEmployeeDefaultHoursRejectsDuplicateDayOfWeek() {
    DefaultWeekHoursRequest req = new DefaultWeekHoursRequest();
    req.setDays(List.of(dayRequest(1, 9, 0, 17, 0), dayRequest(1, 10, 0, 18, 0)));

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> defaultHoursService.setEmployeeDefaultHours(10L, req, 99L),
        "Duplicate dayOfWeek");
  }

  @Test
  void setEmployeeDefaultHoursCreatesWeekScheduleOnFirstUse() {
    DefaultWeekHoursRequest req = new DefaultWeekHoursRequest();
    req.setDays(List.of(dayRequest(1, 9, 0, 17, 0)));
    when(employeeApi.getWeekScheduleId(10L)).thenReturn(null);
    when(weekScheduleRepository.save(any(WeekSchedule.class))).thenReturn(weekSchedule(100L));
    when(scheduleBlockRepository.findAllBySetIdOrderByDayOfWeekAsc(100L))
        .thenReturn(List.of(new ScheduleBlock(100L, 1, LocalTime.of(9, 0), LocalTime.of(17, 0))));

    DefaultWeekHoursResponse response = defaultHoursService.setEmployeeDefaultHours(10L, req, 99L);

    verify(employeeApi).assignWeekSchedule(10L, 100L);
    assertThat(response.getDays()).hasSize(1);
    assertThat(response.getDays().getFirst().getDayOfWeek()).isEqualTo(1);
  }

  @Test
  void inheritDepartmentScheduleReturnsNullWhenDepartmentHasNoSchedule() {
    when(departmentApi.getWeekScheduleId(10L)).thenReturn(null);

    Long inherited = defaultHoursService.inheritDepartmentSchedule(10L);

    assertThat(inherited).isNull();
  }

  @Test
  void findEmployeeIntervalForDayFallsBackToDepartmentSchedule() {
    when(employeeApi.getWeekScheduleId(10L)).thenReturn(null);
    when(employeeApi.findSummaryById(10L))
        .thenReturn(Optional.of(new EmployeeSummaryResponse(10L, "E", "M", 1L, "Dept", true)));
    when(departmentApi.getWeekScheduleId(1L)).thenReturn(200L);
    when(scheduleBlockRepository.findBySetIdAndDayOfWeek(200L, 2))
        .thenReturn(
            Optional.of(new ScheduleBlock(200L, 2, LocalTime.of(8, 0), LocalTime.of(16, 0))));

    Optional<DefaultDayIntervalResponse> interval =
        defaultHoursService.findEmployeeIntervalForDay(10L, 2);

    assertThat(interval).isPresent();
    assertThat(interval.get().getStartTime()).isEqualTo(LocalTime.of(8, 0));
    assertThat(interval.get().getEndTime()).isEqualTo(LocalTime.of(16, 0));
  }

  private DefaultWeekHoursDayRequest dayRequest(
      int dayOfWeek, int startHour, int startMinute, int endHour, int endMinute) {
    TimeIntervalRequest interval = new TimeIntervalRequest();
    interval.setStartTime(LocalTime.of(startHour, startMinute));
    interval.setEndTime(LocalTime.of(endHour, endMinute));

    DefaultWeekHoursDayRequest day = new DefaultWeekHoursDayRequest();
    day.setDayOfWeek(dayOfWeek);
    day.setIntervals(List.of(interval));
    return day;
  }

  private WeekSchedule weekSchedule(Long id) {
    WeekSchedule set = new WeekSchedule();
    ReflectionTestUtils.setField(set, "id", id);
    return set;
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
