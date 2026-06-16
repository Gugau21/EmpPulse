package com.oman.EmpPulse.user.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.oman.EmpPulse.user.internal.Employee;
import com.oman.EmpPulse.defaulthours.internal.WeekSchedule;

public interface EmployeeApi {

  /**
   * Checks whether any employee is currently assigned to the given department.
   *
   * <p>Used to guard department deletion: a department with employees still assigned to it cannot
   * be removed.
   *
   * @param departmentId the department ID to check
   * @return true if at least one employee is assigned to the department, false otherwise
   */
  boolean hasEmployeesInDepartment(Long departmentId);

  /**
   * Finds an employee and returns their summary (name, surname, department, activity).
   *
   * @param employeeId the employee ID to look up
   * @return the employee summary, or empty if no employee with this ID exists
   */
  Optional<EmployeeSummaryResponse> findSummaryById(Long employeeId);

  /**
   * Finds the summaries of all employees matching the given IDs in one batch. Does not throw if
   * some IDs do not exist; they are simply absent from the result.
   *
   * @param employeeIds the employee IDs to look up
   * @return a map from employee ID to summary, containing only the employees that were found
   */
  Map<Long, EmployeeSummaryResponse> findSummariesByIds(Collection<Long> employeeIds);

  /**
   * Verifies that the given admin may act on the employee and returns the employee's summary.
   * Access is granted only when the admin oversees the employee's department.
   *
   * @param adminId the user ID of the admin requesting access
   * @param employeeId the employee being accessed
   * @return the employee's summary
   * @throws org.springframework.web.server.ResponseStatusException 404 if the employee does not
   *     exist, 403 if the admin does not oversee the employee's department
   */
  EmployeeSummaryResponse requireAdminAccessToEmployee(Long adminId, Long employeeId);

  /**
   * Returns the ID of the week schedule currently linked to the employee, or null if none is set.
   * The caller is expected to have already verified that the employee exists.
   *
   * @param employeeId the employee ID
   * @return the linked week schedule ID, or null if the employee has no schedule
   */
  Long getWeekScheduleId(Long employeeId);

  /**
   * Links the given week schedule to the employee.
   *
   * @param employeeId the employee ID
   * @param weekScheduleId the week schedule ID to assign
   */
  void assignWeekSchedule(Long employeeId, Long weekScheduleId);

  /**
   * Returns the IDs of all active employees (those assigned to a department).
   *
   * @return list of active employee IDs
   */
  List<Long> findActiveEmployeeIds();

  /**
   * Finds all employees assigned to any of the given departments.
   *
   * @param departmentIds the department IDs to look up
   * @return a collection of Employee entities, possibly empty if no employees are found
   */
  Collection<Employee> findAllByDepartmentIdIn(Collection<Long> departmentIds);

  /**
   * Finds the week schedules for the specified employees.
   *
   * @param employeeIds the IDs of the employees whose week schedules to retrieve
   * @return the list of week schedules for the specified employees
   */
  List<WeekSchedule> findWeekScheduleByEmployeeIds(Collection<Long> employeeIds);
}
