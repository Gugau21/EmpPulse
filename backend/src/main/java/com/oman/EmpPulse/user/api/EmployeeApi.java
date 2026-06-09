package com.oman.EmpPulse.user.api;

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
}
