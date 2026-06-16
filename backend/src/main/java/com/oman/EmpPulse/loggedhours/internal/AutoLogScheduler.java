package com.oman.EmpPulse.loggedhours.internal;

import com.oman.EmpPulse.defaulthours.api.DefaultHoursApi;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutoLogScheduler {

  private static final Logger log = LoggerFactory.getLogger(AutoLogScheduler.class);

  private final EmployeeApi employeeApi;
  private final DefaultHoursApi defaultHoursApi;
  private final LeaveApi leaveApi;
  private final AdminApi adminApi;
  private final LoggedHoursService loggedHoursService;
  private final ZoneId zoneId;

  public AutoLogScheduler(
      EmployeeApi employeeApi,
      DefaultHoursApi defaultHoursApi,
      LeaveApi leaveApi,
      AdminApi adminApi,
      LoggedHoursService loggedHoursService,
      @Value("${emppulse.autolog.zone}") String zone) {
    this.employeeApi = employeeApi;
    this.defaultHoursApi = defaultHoursApi;
    this.leaveApi = leaveApi;
    this.adminApi = adminApi;
    this.loggedHoursService = loggedHoursService;
    this.zoneId = ZoneId.of(zone);
  }

  @Scheduled(cron = "${emppulse.autolog.cron}", zone = "${emppulse.autolog.zone}")
  public void scheduledAutoLog() {
    autoLogForToday();
  }

  void autoLogForToday() {
    LocalDate today = LocalDate.now(zoneId);
    int dayOfWeek = today.getDayOfWeek().getValue() - 1;

    Long ownerAdminId =
        adminApi
            .getOwnerAdmin()
            .map(admin -> admin.getId())
            .orElseGet(
                () -> {
                  log.warn("Skipping auto-log: owner admin not found");
                  return null;
                });
    if (ownerAdminId == null) {
      return;
    }

    List<Long> employeeIds = employeeApi.findActiveEmployeeIds();
    if (employeeIds.isEmpty()) {
      return;
    }

    Set<Long> unpaidLeaveEmployeeIds =
        leaveApi.findEmployeeIdsOnUnpaidApprovedLeave(employeeIds, today);

    for (Long employeeId : employeeIds) {
      try {
        if (unpaidLeaveEmployeeIds.contains(employeeId)) {
          continue;
        }

        defaultHoursApi
            .findEmployeeIntervalForDay(employeeId, dayOfWeek)
            .ifPresent(
                interval ->
                    loggedHoursService.createAutoLogIfAbsent(
                        employeeId,
                        ownerAdminId,
                        today,
                        interval.getStartTime(),
                        interval.getEndTime()));
      } catch (Exception e) {
        log.error("Auto-log failed for employee {}", employeeId, e);
      }
    }
  }
}
