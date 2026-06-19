package com.oman.EmpPulse.defaulthours.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DefaultHoursApi {

  /**
   * Creates a fresh week schedule that copies the given department's current default hours, for a
   * newly created employee to inherit. The copy is independent: later overriding the employee's
   * hours (or the department's) does not affect the other.
   *
   * @param departmentId the department whose default hours the employee inherits
   * @return the ID of the newly created week schedule, or null if the department has no default
   *     hours to inherit
   */
  Long inheritDepartmentSchedule(Long departmentId);

  /**
   * Returns the effective default working interval for an employee on a given day of week.
   *
   * <p>Uses the employee's own week schedule when set; otherwise falls back to the employee's
   * department schedule. {@code dayOfWeek} follows the schedule_block convention: 0 = Monday … 6 =
   * Sunday.
   *
   * @param employeeId the employee whose interval to resolve
   * @param dayOfWeek the day index (0–6)
   * @return the interval when a schedule block exists for that day, otherwise empty
   */
  Optional<DefaultDayIntervalResponse> findEmployeeIntervalForDay(Long employeeId, int dayOfWeek);

  /**
   * Returns the schedule blocks for the specified week schedules.
   *
   * @param weekScheduleIds the IDs of the week schedules whose schedule blocks to retrieve
   * @return the list of schedule blocks for the specified week schedules
   */
  List<ScheduleBlock> findEmployeeScheduleBlocks(Collection<Long> weekScheduleIds);
}
