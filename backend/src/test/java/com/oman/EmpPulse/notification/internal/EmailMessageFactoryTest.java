package com.oman.EmpPulse.notification.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.oman.EmpPulse.notification.api.LeaveNotificationDetails;
import com.oman.EmpPulse.notification.api.NotificationRecipient;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmailMessageFactoryTest {

  private EmailMessageFactory emailMessageFactory;

  @BeforeEach
  void setUp() {
    emailMessageFactory = new EmailMessageFactory("http://localhost");
  }

  @Test
  void accountCredentialsContainsRecipientEmailAndPassword() {
    EmailMessageFactory.EmailMessage message =
        emailMessageFactory.accountCredentials(
            new NotificationRecipient("user@x.com", "User"), "temp-pass");

    assertThat(message.subject()).contains("account credentials");
    assertThat(message.body()).contains("user@x.com");
    assertThat(message.body()).contains("temp-pass");
    assertThat(message.body()).contains("http://localhost");
  }

  @Test
  void leaveDecisionContainsDecisionDetails() {
    LeaveNotificationDetails details =
        new LeaveNotificationDetails(
            "vacation", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), false);
    EmailMessageFactory.EmailMessage message =
        emailMessageFactory.leaveDecision(
            new NotificationRecipient("user@x.com", "User"), details, false);

    assertThat(message.subject()).contains("rejected");
    assertThat(message.body()).contains("vacation");
    assertThat(message.body()).contains("No");
    assertThat(message.body()).contains("2026-06-01");
    assertThat(message.body()).contains("2026-06-02");
  }

  @Test
  void passwordResetContainsResetLink() {
    EmailMessageFactory.EmailMessage message =
        emailMessageFactory.passwordReset(
            new NotificationRecipient("user@x.com", "User"),
            "http://localhost/reset-password?token=abc");

    assertThat(message.subject()).contains("Reset your EmpPulse password");
    assertThat(message.body()).contains("token=abc");
    assertThat(message.body()).contains("15 minutes");
  }
}
