package com.oman.EmpPulse.user.api;

import java.util.Collection;
import com.oman.EmpPulse.user.internal.Employee;

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
   * Finds all employees assigned to any of the given departments.
   *
   * @param departmentIds the department IDs to look up
   * @return a collection of Employee entities, possibly empty if no employees are found
   */
  Collection<Employee> findAllByDepartmentIdIn(Collection<Long> departmentIds);
}
