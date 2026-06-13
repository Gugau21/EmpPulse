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

  /**
   * Ensures a Default Department exists within the system. This department cannot be deleted from
   * the system.
   *
   * <p>Called during bootstrap to seed the application with a Default Department. If a department
   * with {@code is_default = true} already exists, the method returns without modification.
   *
   * <p>Must run before {@link com.oman.EmpPulse.user.api.UserApi#ensureOwnerExists} so that the
   * owner is automatically assigned to the default department when the owner account is created.
   */
  void ensureDefaultDepartmentExists();

  /**
   * Finds all departments in the system. Owners get all departments; admins get only those they
   * oversee.
   *
   * @return the list of departments the caller has access to
   */
  Collection<Department> findAll();

  /**
   * Finds all departments matching the given IDs. Does not throw if some IDs do not exist. Returns
   * only the found departments.
   *
   * @param ids the department IDs to look up
   * @return a list of Department entities, possibly smaller than the input if some IDs were not
   *     found
   */
  Collection<Department> findAllByIds(Collection<Long> ids);
}
