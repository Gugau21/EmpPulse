package com.oman.EmpPulse.user.api;

import java.util.Optional;

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
}
