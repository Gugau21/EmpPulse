package com.oman.EmpPulse.notification.internal;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.oman.EmpPulse.notification.api.LeaveNotificationDetails;
import com.oman.EmpPulse.notification.api.NotificationRecipient;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private EmailMessageFactory messageFactory;
  @Mock private EmailSender emailSender;

  @InjectMocks private NotificationService notificationService;

  @Test
  void sendAccountCredentialsDelegatesToFactoryAndSender() {
    NotificationRecipient recipient = new NotificationRecipient("user@x.com", "User");
    EmailMessageFactory.EmailMessage message =
        new EmailMessageFactory.EmailMessage("subject", "body");
    when(messageFactory.accountCredentials(recipient, "pass")).thenReturn(message);

    notificationService.sendAccountCredentials(recipient, "pass");

    verify(emailSender).send("user@x.com", "subject", "body");
  }

  @Test
  void sendSafelySwallowsExceptions() {
    NotificationRecipient recipient = new NotificationRecipient("user@x.com", "User");
    LeaveNotificationDetails details =
        new LeaveNotificationDetails(
            "vacation", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3), true);
    when(messageFactory.leaveDecision(recipient, details, true))
        .thenReturn(new EmailMessageFactory.EmailMessage("subject", "body"));
    doThrow(new RuntimeException("smtp down"))
        .when(emailSender)
        .send("user@x.com", "subject", "body");

    Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
    Level previousLevel = logger.getLevel();
    try {
      // This test intentionally triggers the error path; mute expected log noise.
      logger.setLevel(Level.OFF);
      notificationService.sendLeaveDecision(recipient, details, true);
    } finally {
      logger.setLevel(previousLevel);
    }

    verify(messageFactory).leaveDecision(recipient, details, true);
  }
}
