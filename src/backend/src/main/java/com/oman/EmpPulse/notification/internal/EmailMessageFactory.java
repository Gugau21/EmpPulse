package com.oman.EmpPulse.notification.internal;

import com.oman.EmpPulse.notification.api.LeaveNotificationDetails;
import com.oman.EmpPulse.notification.api.NotificationRecipient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailMessageFactory {

  private final String appBaseUrl;

  public EmailMessageFactory(@Value("${app.base-url}") String appBaseUrl) {
    this.appBaseUrl = appBaseUrl;
  }

  public EmailMessage accountCredentials(NotificationRecipient recipient, String rawPassword) {
    return new EmailMessage(
        "Your EmpPulse account credentials",
        """
        Hello %s,

        Your EmpPulse account has been created.

        Email: %s
        Password: %s

        Sign in at: %s

        Please change your password after your first login.
        """
            .formatted(recipient.name(), recipient.email(), rawPassword, appBaseUrl));
  }

  public EmailMessage passwordChanged(NotificationRecipient recipient) {
    return new EmailMessage(
        "Your EmpPulse password was changed",
        """
        Hello %s,

        Your EmpPulse account password was changed successfully.

        If you did not make this change, contact your administrator immediately.

        Sign in at: %s
        """
            .formatted(recipient.name(), appBaseUrl));
  }

  public EmailMessage emailChanged(NotificationRecipient recipient) {
    return new EmailMessage(
        "Your EmpPulse email address was updated",
        """
        Hello %s,

        Your EmpPulse account email address was updated to this address.

        Sign in at: %s
        """
            .formatted(recipient.name(), appBaseUrl));
  }

  public EmailMessage leaveCreatedOnBehalf(
      NotificationRecipient recipient, LeaveNotificationDetails details) {
    return new EmailMessage(
        "A leave request was created on your behalf",
        """
        Hello %s,

        An administrator created a leave request on your behalf.

        Type: %s
        Dates: %s to %s
        Paid: %s

        View your requests at: %s
        """
            .formatted(
                recipient.name(),
                details.leaveType(),
                details.startDate(),
                details.endDate(),
                paidLabel(details.paid()),
                appBaseUrl));
  }

  public EmailMessage leaveDecision(
      NotificationRecipient recipient, LeaveNotificationDetails details, boolean approved) {
    String status = approved ? "approved" : "rejected";
    return new EmailMessage(
        "Your leave request was " + status,
        """
        Hello %s,

        Your leave request has been %s.

        Type: %s
        Dates: %s to %s
        Paid: %s

        View your requests at: %s
        """
            .formatted(
                recipient.name(),
                status,
                details.leaveType(),
                details.startDate(),
                details.endDate(),
                paidLabel(details.paid()),
                appBaseUrl));
  }

  public EmailMessage leaveModifiedByAdmin(
      NotificationRecipient recipient, LeaveNotificationDetails details) {
    return new EmailMessage(
        "Your leave request was updated",
        """
        Hello %s,

        An administrator updated your leave request.

        Type: %s
        Dates: %s to %s
        Paid: %s

        View your requests at: %s
        """
            .formatted(
                recipient.name(),
                details.leaveType(),
                details.startDate(),
                details.endDate(),
                paidLabel(details.paid()),
                appBaseUrl));
  }

  public EmailMessage passwordReset(NotificationRecipient recipient, String resetLink) {
    return new EmailMessage(
        "Reset your EmpPulse password",
        """
        Hello %s,

        We received a request to reset your EmpPulse password.

        Follow this link to choose a new password:
        %s

        This link expires in 15 minutes. If you did not request a reset, you can ignore this email.

        Sign in at: %s
        """
            .formatted(recipient.name(), resetLink, appBaseUrl));
  }

  private String paidLabel(boolean paid) {
    return paid ? "Yes" : "No";
  }

  public record EmailMessage(String subject, String body) {}
}
