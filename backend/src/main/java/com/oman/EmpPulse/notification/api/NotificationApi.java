package com.oman.EmpPulse.notification.api;

/**
 * Public API for sending transactional emails. All methods are fire-and-forget: callers are not
 * blocked and SMTP failures do not propagate.
 */
public interface NotificationApi {

  /**
   * Sends account credentials to a newly registered user, or when an owner sets a new password.
   *
   * @param recipient the user's email and display name
   * @param rawPassword the plaintext password to include in the email
   */
  void sendAccountCredentials(NotificationRecipient recipient, String rawPassword);

  /**
   * Notifies a user that their password was changed while logged in.
   *
   * @param recipient the user's email and display name
   */
  void sendPasswordChangedNotification(NotificationRecipient recipient);

  /**
   * Notifies a user that their email address was changed by an owner. Sent to the new address.
   *
   * @param recipient the user's new email and display name
   */
  void sendEmailChangedNotification(NotificationRecipient recipient);

  /**
   * Notifies an employee that a leave request was created on their behalf by an administrator.
   *
   * @param recipient the employee's email and display name
   * @param details leave type, dates, and paid status
   */
  void sendLeaveCreatedOnBehalf(NotificationRecipient recipient, LeaveNotificationDetails details);

  /**
   * Notifies an employee that their leave request was approved or rejected.
   *
   * @param recipient the employee's email and display name
   * @param details leave type, dates, and paid status
   * @param approved true if approved, false if rejected
   */
  void sendLeaveDecision(
      NotificationRecipient recipient, LeaveNotificationDetails details, boolean approved);

  /**
   * Notifies an employee that their leave request was modified by an administrator.
   *
   * @param recipient the employee's email and display name
   * @param details the updated leave type, dates, and paid status
   */
  void sendLeaveModifiedByAdmin(NotificationRecipient recipient, LeaveNotificationDetails details);

  /**
   * Sends a password reset link email. Token generation and validation are handled elsewhere.
   *
   * @param recipient the user's email and display name
   * @param resetLink the full URL the user should follow to reset their password
   */
  void sendPasswordResetEmail(NotificationRecipient recipient, String resetLink);
}
