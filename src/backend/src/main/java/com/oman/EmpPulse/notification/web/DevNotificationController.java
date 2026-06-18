package com.oman.EmpPulse.notification.web;

import com.oman.EmpPulse.notification.api.LeaveNotificationDetails;
import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.notification.api.NotificationRecipient;
import com.oman.EmpPulse.notification.dto.DevTestEmailRequest;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dev/notifications")
@ConditionalOnProperty(name = "app.dev.test-email.enabled", havingValue = "true")
public class DevNotificationController {

  private final NotificationApi notificationApi;
  private final String appBaseUrl;

  public DevNotificationController(
      NotificationApi notificationApi, @Value("${app.base-url}") String appBaseUrl) {
    this.notificationApi = notificationApi;
    this.appBaseUrl = appBaseUrl;
  }

  @PostMapping("/test")
  public ResponseEntity<Map<String, String>> sendTestEmail(@RequestBody DevTestEmailRequest req) {
    if (!StringUtils.hasText(req.getEmail()) || !StringUtils.hasText(req.getName())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email and name are required");
    }

    NotificationRecipient recipient = new NotificationRecipient(req.getEmail(), req.getName());
    String type = req.getType() != null ? req.getType() : "credentials";

    switch (type) {
      case "credentials" -> notificationApi.sendAccountCredentials(recipient, "test-password-123");
      case "password-changed" -> notificationApi.sendPasswordChangedNotification(recipient);
      case "email-changed" -> notificationApi.sendEmailChangedNotification(recipient);
      case "leave-on-behalf" ->
          notificationApi.sendLeaveCreatedOnBehalf(recipient, sampleLeaveDetails());
      case "leave-decision-approved" ->
          notificationApi.sendLeaveDecision(recipient, sampleLeaveDetails(), true);
      case "leave-decision-rejected" ->
          notificationApi.sendLeaveDecision(recipient, sampleLeaveDetails(), false);
      case "leave-modified" ->
          notificationApi.sendLeaveModifiedByAdmin(recipient, sampleLeaveDetails());
      case "password-reset" ->
          notificationApi.sendPasswordResetEmail(recipient, appBaseUrl + "/reset?token=test-token");
      default ->
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Unknown type. Use: credentials, password-changed, email-changed, leave-on-behalf,"
                  + " leave-decision-approved, leave-decision-rejected, leave-modified,"
                  + " password-reset");
    }

    return ResponseEntity.accepted()
        .body(Map.of("message", "Test email queued", "type", type, "to", req.getEmail()));
  }

  private LeaveNotificationDetails sampleLeaveDetails() {
    return new LeaveNotificationDetails(
        "Vacation", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5), true);
  }
}
