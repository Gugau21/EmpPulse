package com.oman.EmpPulse.user.api;

import com.oman.EmpPulse.user.internal.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserApi {

  /**
   * Looks up an active (non-deleted) user by email and returns their credential including password
   * hash and authority list.
   *
   * <p>Used by the auth module to authenticate login requests. Returns empty if the user does not
   * exist or has been soft-deleted.
   *
   * @param email the user's email address
   * @return a UserCredential with (id, passwordHash, authorities), or empty if not found or deleted
   */
  Optional<UserCredential> findActiveByEmail(String email);

  /**
   * Loads the full profile for a user, including their personal info, preferences, and role details
   * (departments for admins, assigned department for employees).
   *
   * @param userId the user ID
   * @return a populated UserResponse; throws 404 if the user does not exist
   */
  UserResponse loadProfile(Long userId);

  /**
   * Looks up a user's contact details (email and first name) for addressing a notification email.
   *
   * <p>Intentionally lightweight: unlike {@link #loadProfile}, it does not assemble role details or
   * vacation balances, so it is safe to call from other modules (e.g. the leave module) without
   * re-entering them.
   *
   * @param userId the user ID
   * @return the user's contact details, or empty if no user with this ID exists
   */
  Optional<UserContact> findContactById(Long userId);

  /**
   * Sets a new password for the given user and invalidates all of their active sessions.
   *
   * <p>Used by the forgot-password flow ({@code POST /api/auth/password/reset}) once a reset token
   * has been validated. Unlike the logged-in change ({@code POST /api/me/password/change}), this
   * does not require the current password and clears <em>every</em> session, so the user must log
   * in again.
   *
   * @param userId the user whose password is being reset
   * @param rawNewPassword the new password in plaintext
   */
  void resetPassword(Long userId, String rawNewPassword);

  /**
   * Ensures an owner account exists with the given email and password, creating it if needed.
   *
   * <p>Called during bootstrap to seed the application with an initial owner. If an owner with this
   * email already exists, the method returns without modification.
   *
   * <p>Must run after {@link
   * com.oman.EmpPulse.department.api.DepartmentApi#ensureDefaultDepartmentExists} so that the owner
   * is assigned to the default department on creation.
   *
   * @param email the owner's email address
   * @param rawPassword the owner's password in plaintext.
   */
  void ensureOwnerExists(String email, String rawPassword);

  /**
   * Finds users by their IDs.
   *
   * @param userIds the collection of user IDs
   * @return a list of users with the specified IDs
   */
  List<User> findByIdIn(Collection<Long> userIds);

  /**
   * Finds a user by their ID.
   *
   * @param userId the user ID
   * @return an Optional containing the user if found, or empty if not found
   */
  Optional<User> findById(Long userId);
}
