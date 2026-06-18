package com.oman.EmpPulse.user.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AdminApi {

  /**
   * Finds all admins matching the given IDs. Does not throw if some IDs do not exist. Returns only
   * the found admins.
   *
   * @param ids the admin IDs to look up
   * @return a list of Admin entities, possibly smaller than the input if some IDs were not found
   */
  List<Admin> findAllByIds(Collection<Long> ids);

  /**
   * Finds an admin by ID.
   *
   * @param id the admin ID
   * @return the admin, or empty if not found
   */
  Optional<Admin> findById(Long id);

  /**
   * Returns the owner's admin row. The owner is a real admin of every department, so this is used
   * to add, preserve, and identify the owner when managing department rosters.
   *
   * @return the owner's admin entity, or empty if the owner has not been seeded yet
   */
  Optional<Admin> getOwnerAdmin();

  /**
   * Converts an Admin entity to a response DTO.
   *
   * @param admin the admin entity to convert
   * @return the response DTO
   */
  AdminSummaryResponse toAdminSummaryResponse(Admin admin);

  /**
   * Gets the IDs of all departments this admin oversees.
   *
   * @param userId the user ID of the admin
   * @return the list of department IDs this admin is assigned to
   */
  List<Long> departmentIdsForAdminUser(Long userId);

  /**
   * Checks if the given user (who is an admin) has authority over the given department.
   *
   * @param userId the user ID of the admin
   * @param departmentId the department ID to check
   * @return true if this admin oversees the department, false otherwise
   */
  boolean overseesDepartment(Long userId, Long departmentId);

  /**
   * Finds all admins assigned to any of the given departments.
   *
   * @param departmentIds the department IDs to look up
   * @return a collection of Admin entities, possibly empty if no admins are found
   */
  Collection<Admin> findAllByDepartmentIdIn(Collection<Long> departmentIds);
}
