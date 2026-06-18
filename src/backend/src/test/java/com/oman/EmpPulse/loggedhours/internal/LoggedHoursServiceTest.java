package com.oman.EmpPulse.loggedhours.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.loggedhours.dto.LoggedHoursCreateRequest;
import com.oman.EmpPulse.loggedhours.dto.LoggedHoursResponse;
import com.oman.EmpPulse.loggedhours.dto.LoggedHoursUpdateRequest;
import com.oman.EmpPulse.user.api.EmployeeApi;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LoggedHoursServiceTest {

  @Mock private LoggedHoursRepository loggedHoursRepository;
  @Mock private EmployeeApi employeeApi;

  @InjectMocks private LoggedHoursService loggedHoursService;

  @Test
  void createLoggedHoursRejectsFutureDate() {
    LoggedHoursCreateRequest req = new LoggedHoursCreateRequest();
    req.setDate(LocalDate.now().plusDays(1));
    req.setStartTime(LocalTime.of(9, 0));
    req.setEndTime(LocalTime.of(10, 0));

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> loggedHoursService.createLoggedHours(10L, req, 99L),
        "future date");
  }

  @Test
  void createLoggedHoursMergesOverlappingIntervals() {
    LoggedHoursCreateRequest req = new LoggedHoursCreateRequest();
    req.setDate(LocalDate.of(2026, 6, 1));
    req.setStartTime(LocalTime.of(10, 0));
    req.setEndTime(LocalTime.of(13, 0));

    LoggedHours existing =
        entry(1L, 10L, 99L, req.getDate(), LocalTime.of(9, 0), LocalTime.of(11, 0));
    when(loggedHoursRepository.findAllByEmployeeIdAndDate(10L, req.getDate()))
        .thenReturn(List.of(existing));
    when(loggedHoursRepository.save(any(LoggedHours.class)))
        .thenAnswer(
            invocation ->
                entry(
                    2L,
                    10L,
                    99L,
                    req.getDate(),
                    invocation.<LoggedHours>getArgument(0).getStartTime(),
                    invocation.<LoggedHours>getArgument(0).getEndTime()));

    LoggedHoursResponse response = loggedHoursService.createLoggedHours(10L, req, 99L);

    assertThat(response.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    assertThat(response.getEndTime()).isEqualTo(LocalTime.of(13, 0));
    verify(loggedHoursRepository).deleteAll(List.of(existing));
  }

  @Test
  void listLoggedHoursRejectsDifferentEmployeeForNonAdminCaller() {
    assertStatus(
        HttpStatus.FORBIDDEN,
        () -> loggedHoursService.listLoggedHours(10L, 50L, false),
        "No access to this employee");
  }

  @Test
  void updateLoggedHoursRejectsInvalidTimeRange() {
    LoggedHoursUpdateRequest req = new LoggedHoursUpdateRequest();
    req.setDate(LocalDate.of(2026, 6, 1));
    req.setStartTime(LocalTime.of(12, 0));
    req.setEndTime(LocalTime.of(12, 0));

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> loggedHoursService.updateLoggedHours(10L, 1L, req, 99L),
        "Start time must be before end time");
  }

  @Test
  void createAutoLogIfAbsentSkipsWhenEntryAlreadyExists() {
    when(loggedHoursRepository.findAllByEmployeeIdAndDate(10L, LocalDate.of(2026, 6, 1)))
        .thenReturn(
            List.of(
                entry(
                    1L,
                    10L,
                    99L,
                    LocalDate.of(2026, 6, 1),
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0))));

    loggedHoursService.createAutoLogIfAbsent(
        10L, 99L, LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(17, 0));

    verify(loggedHoursRepository, org.mockito.Mockito.never()).save(any(LoggedHours.class));
  }

  private LoggedHours entry(
      Long id, Long employeeId, Long adminId, LocalDate date, LocalTime start, LocalTime end) {
    LoggedHours loggedHours = new LoggedHours(employeeId, adminId, date, start, end);
    ReflectionTestUtils.setField(loggedHours, "id", id);
    return loggedHours;
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
