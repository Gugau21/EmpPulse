package com.oman.EmpPulse.defaulthours.api;

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
}
