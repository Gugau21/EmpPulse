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
   * Converts an Admin entity to a response DTO. Returns null if the admin has been soft-deleted.
   *
   * @param admin the admin entity to convert
   * @return the response DTO, or null if the admin is deleted
   */
  AdminSummaryResponse toSummary(Admin admin);

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
}
