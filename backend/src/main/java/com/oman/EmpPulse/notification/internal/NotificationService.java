package com.oman.EmpPulse.notification.internal;

import com.oman.EmpPulse.notification.api.LeaveNotificationDetails;
import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.notification.api.NotificationRecipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements NotificationApi {

  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

  private final EmailMessageFactory messageFactory;
  private final EmailSender emailSender;

  public NotificationService(EmailMessageFactory messageFactory, EmailSender emailSender) {
    this.messageFactory = messageFactory;
    this.emailSender = emailSender;
  }

  @Override
  @Async
  public void sendAccountCredentials(NotificationRecipient recipient, String rawPassword) {
    sendSafely(
        recipient,
        "account credentials",
        () -> messageFactory.accountCredentials(recipient, rawPassword));
  }

  @Override
  @Async
  public void sendPasswordChangedNotification(NotificationRecipient recipient) {
    sendSafely(recipient, "password changed", () -> messageFactory.passwordChanged(recipient));
  }

  @Override
  @Async
  public void sendEmailChangedNotification(NotificationRecipient recipient) {
    sendSafely(recipient, "email changed", () -> messageFactory.emailChanged(recipient));
  }

  @Override
  @Async
  public void sendLeaveCreatedOnBehalf(
      NotificationRecipient recipient, LeaveNotificationDetails details) {
    sendSafely(
        recipient,
        "leave created on behalf",
        () -> messageFactory.leaveCreatedOnBehalf(recipient, details));
  }

  @Override
  @Async
  public void sendLeaveDecision(
      NotificationRecipient recipient, LeaveNotificationDetails details, boolean approved) {
    sendSafely(
        recipient,
        approved ? "leave approved" : "leave rejected",
        () -> messageFactory.leaveDecision(recipient, details, approved));
  }

  @Override
  @Async
  public void sendLeaveModifiedByAdmin(
      NotificationRecipient recipient, LeaveNotificationDetails details) {
    sendSafely(
        recipient,
        "leave modified by admin",
        () -> messageFactory.leaveModifiedByAdmin(recipient, details));
  }

  @Override
  @Async
  public void sendPasswordResetEmail(NotificationRecipient recipient, String resetLink) {
    sendSafely(
        recipient, "password reset", () -> messageFactory.passwordReset(recipient, resetLink));
  }

  private void sendSafely(
      NotificationRecipient recipient, String emailType, EmailMessageSupplier supplier) {
    try {
      EmailMessageFactory.EmailMessage message = supplier.get();
      emailSender.send(recipient.email(), message.subject(), message.body());
    } catch (Exception ex) {
      log.error(
          "Failed to send {} email to {}: {}", emailType, recipient.email(), ex.getMessage(), ex);
    }
  }

  @FunctionalInterface
  private interface EmailMessageSupplier {
    EmailMessageFactory.EmailMessage get();
  }
}
