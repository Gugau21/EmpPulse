package com.oman.EmpPulse.user.api;

import java.util.Optional;

public interface UserDirectory {

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
   * Ensures an owner account exists with the given email and password, creating it if needed..
   *
   * <p>Called during bootstrap to seed the application with an initial owner. If an owner with this
   * email already exists, the method returns without modification.
   *
   * @param email the owner's email address
   * @param rawPassword the owner's password in plaintext.
   */
  void ensureOwnerExists(String email, String rawPassword);
}
