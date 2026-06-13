package com.oman.EmpPulse.department.api;

import java.util.Collection;
import java.util.List;
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
   * Returns the IDs of all departments. Used to attach the owner to every department during
   * seeding.
   *
   * @return the list of all department IDs
   */
  List<Long> findAllDepartmentIds();

  /**
   * Replaces the full set of departments an existing admin oversees with the given set, mutating
   * the owning side of the Admin_Department join table. The admin is detached from any current
   * department not in {@code departmentIds} and attached to any that are new.
   *
   * <p>An empty {@code departmentIds} detaches the admin from every department. This will set the
   * {@code Admin.active} to false in the database
   *
   * <p>Callers must be Transactional.
   *
   * @param adminId the ID of the admin to update; throws {@code 500 INTERNAL_SERVER_ERROR} if not
   *     found (since a missing admin at this point indicates a data inconsistency, not a user
   *     error)
   * @param departmentIds the IDs of the departments the admin should oversee after the call; throws
   *     {@code 404 NOT_FOUND} if any ID does not correspond to an existing department
   */
  void setAdminDepartments(Long adminId, Collection<Long> departmentIds);
}
