package com.oman.EmpPulse.user.api;

/**
 * Minimal contact details for a user, used to address transactional emails without loading a full
 * profile.
 *
 * @param email the user's current email address
 * @param name the user's first name, used as the email greeting
 */
public record UserContact(String email, String name) {}
