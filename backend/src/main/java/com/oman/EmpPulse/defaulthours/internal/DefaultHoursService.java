package com.oman.EmpPulse.defaulthours.internal;

import com.oman.EmpPulse.defaulthours.dto.DefaultWeekHoursDayRequest;
import com.oman.EmpPulse.defaulthours.dto.DefaultWeekHoursDayResponse;
import com.oman.EmpPulse.defaulthours.dto.DefaultWeekHoursRequest;
import com.oman.EmpPulse.defaulthours.dto.DefaultWeekHoursResponse;
import com.oman.EmpPulse.defaulthours.dto.TimeIntervalRequest;
import com.oman.EmpPulse.defaulthours.dto.TimeIntervalResponse;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.LongConsumer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DefaultHoursService {

  private final WeekScheduleRepository weekScheduleRepository;
  private final ScheduleBlockRepository scheduleBlockRepository;
  private final EmployeeApi employeeApi;
  private final DepartmentApi departmentApi;

  public DefaultHoursService(
      WeekScheduleRepository weekScheduleRepository,
      ScheduleBlockRepository scheduleBlockRepository,
      EmployeeApi employeeApi,
      DepartmentApi departmentApi) {
    this.weekScheduleRepository = weekScheduleRepository;
    this.scheduleBlockRepository = scheduleBlockRepository;
    this.employeeApi = employeeApi;
    this.departmentApi = departmentApi;
  }

  /**
   * Replaces an employee's weekly default hours with the given schedule. Each day may carry at most
   * one interval (the underlying table is unique per set/day). The employee's week schedule is
   * created on first use and reused afterwards; its existing blocks are wiped and rewritten.
   *
   * @param employeeId the employee whose default hours to set
   * @param req the schedule payload (days, each with at most one interval)
   * @param callerAdminId the user ID of the authenticated admin
   * @return the saved schedule
   */
  @Transactional
  public DefaultWeekHoursResponse setEmployeeDefaultHours(
      Long employeeId, DefaultWeekHoursRequest req, Long callerAdminId) {
    employeeApi.requireAdminAccessToEmployee(callerAdminId, employeeId);
    return replaceSchedule(
        employeeApi.getWeekScheduleId(employeeId),
        req,
        setId -> employeeApi.assignWeekSchedule(employeeId, setId));
  }

  /**
   * Replaces a department's weekly default hours with the given schedule, following the same rules
   * as {@link #setEmployeeDefaultHours}. A department's default hours are inherited by its
   * employees who have no schedule of their own.
   *
   * @param departmentId the department whose default hours to set
   * @param req the schedule payload (days, each with at most one interval)
   * @param callerAdminId the user ID of the authenticated admin
   * @return the saved schedule
   */
  @Transactional
  public DefaultWeekHoursResponse setDepartmentDefaultHours(
      Long departmentId, DefaultWeekHoursRequest req, Long callerAdminId) {
    departmentApi.requireAdminAccessToDepartment(callerAdminId, departmentId);
    return replaceSchedule(
        departmentApi.getWeekScheduleId(departmentId),
        req,
        setId -> departmentApi.assignWeekSchedule(departmentId, setId));
  }

  /**
   * Validates the request and rewrites the owner's weekly schedule. The owner's week schedule is
   * created on first use (and linked back via {@code scheduleAssigner}) and reused afterwards; its
   * existing blocks are wiped and rewritten. Each day may carry at most one interval (the
   * underlying table is unique per set/day).
   *
   * @param existingSetId the owner's current week schedule ID, or null if none is set yet
   * @param req the schedule payload
   * @param scheduleAssigner links a newly created week schedule back to the owner
   * @return the saved schedule
   */
  private DefaultWeekHoursResponse replaceSchedule(
      Long existingSetId, DefaultWeekHoursRequest req, LongConsumer scheduleAssigner) {
    List<ScheduleBlock> blocks = validateAndBuildBlocks(req);

    Long setId = existingSetId;
    if (setId == null) {
      setId = weekScheduleRepository.save(new WeekSchedule()).getId();
      scheduleAssigner.accept(setId);
    } else {
      scheduleBlockRepository.deleteAllBySetId(setId);
    }

    for (ScheduleBlock block : blocks) {
      scheduleBlockRepository.save(
          new ScheduleBlock(setId, block.getDayOfWeek(), block.getStartTime(), block.getEndTime()));
    }

    return toDefaultWeekHours(setId);
  }

  /**
   * Returns an employee's weekly default hours. An employee that has never had default hours set
   * yields an empty schedule (no days).
   *
   * @param employeeId the employee whose default hours to read
   * @param callerAdminId the user ID of the authenticated admin
   * @return the employee's schedule, empty if none has been set
   */
  @Transactional(readOnly = true)
  public DefaultWeekHoursResponse getEmployeeDefaultHours(Long employeeId, Long callerAdminId) {
    employeeApi.requireAdminAccessToEmployee(callerAdminId, employeeId);

    Long setId = employeeApi.getWeekScheduleId(employeeId);
    if (setId == null) {
      return new DefaultWeekHoursResponse(List.of());
    }
    return toDefaultWeekHours(setId);
  }

  /**
   * Validates the request and turns it into one block per day that carries an interval. The blocks
   * are detached (no set ID yet); the caller assigns the set and persists them.
   */
  private List<ScheduleBlock> validateAndBuildBlocks(DefaultWeekHoursRequest req) {
    if (req == null || req.getDays() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "days is required");
    }

    List<ScheduleBlock> blocks = new ArrayList<>();
    Set<Integer> seenDays = new HashSet<>();
    for (DefaultWeekHoursDayRequest day : req.getDays()) {
      Integer dayOfWeek = day.getDayOfWeek();
      if (dayOfWeek == null || dayOfWeek < 0 || dayOfWeek > 6) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "dayOfWeek must be between 0 and 6");
      }
      if (!seenDays.add(dayOfWeek)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Duplicate dayOfWeek: " + dayOfWeek);
      }
      if (day.getIntervals() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "intervals is required");
      }
      if (day.getIntervals().size() > 1) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Only one interval per day is allowed");
      }
      if (day.getIntervals().isEmpty()) {
        continue;
      }

      TimeIntervalRequest interval = day.getIntervals().get(0);
      if (interval.getStartTime() == null || interval.getEndTime() == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "startTime and endTime are required");
      }
      if (!interval.getStartTime().isBefore(interval.getEndTime())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Start time must be before end time");
      }
      blocks.add(
          new ScheduleBlock(null, dayOfWeek, interval.getStartTime(), interval.getEndTime()));
    }
    return blocks;
  }

  private DefaultWeekHoursResponse toDefaultWeekHours(Long setId) {
    List<DefaultWeekHoursDayResponse> days =
        scheduleBlockRepository.findAllBySetIdOrderByDayOfWeekAsc(setId).stream()
            .map(
                block ->
                    new DefaultWeekHoursDayResponse(
                        block.getDayOfWeek(),
                        List.of(
                            new TimeIntervalResponse(block.getStartTime(), block.getEndTime()))))
            .toList();
    return new DefaultWeekHoursResponse(days);
  }
}
