package com.oman.EmpPulse.department.api;

import java.util.Collection;
import java.util.Optional;

public interface DepartmentApi {

  /**
   * Returns the name of the department if it exists.
   *
   * @param departmentId the ID of the department to look up
   * @return the department name, or empty if not found
   */
  Optional<String> findNameById(Long departmentId);

  /**
   * Assigns an existing admin to one or more departments by adding them to the owning side of the
   * Admin_Department join table.
   *
   * <p>Callers must be Transactional.
   *
   * @param adminId the ID of the admin to assign; throws {@code 500 INTERNAL_SERVER_ERROR} if not
   *     found (since a missing admin at this point indicates a data inconsistency, not a user
   *     error)
   * @param departmentIds the IDs of the departments to assign the admin to; throws {@code 404
   *     NOT_FOUND} if any ID does not correspond to an existing department
   */
  void assignAdminToDepartments(Long adminId, Collection<Long> departmentIds);
}
