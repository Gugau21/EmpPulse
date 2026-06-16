package com.oman.EmpPulse.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.auth.internal.PasswordResetService;
import com.oman.EmpPulse.auth.internal.PasswordResetToken;
import com.oman.EmpPulse.auth.internal.PasswordResetTokenRepository;
import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.notification.api.NotificationRecipient;
import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.UserCredential;
import com.oman.EmpPulse.user.api.UserPreferencesResponse;
import com.oman.EmpPulse.user.api.UserResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Integration test: verifies the full password-reset flow end-to-end at the service level.
 * PasswordResetService coordinates with UserApi (lookup + resetPassword) and NotificationApi
 * (email). Only the repository and external APIs are mocked; the service itself runs with real
 * logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Integration: Password reset request → validate → consume → password changed")
class PasswordResetFlowIT {

  @Mock private PasswordResetTokenRepository tokenRepository;
  @Mock private UserApi userApi;
  @Mock private NotificationApi notificationApi;

  private PasswordResetService passwordResetService;

  @BeforeEach
  void setUp() {
    passwordResetService =
        new PasswordResetService(
            tokenRepository,
            userApi,
            notificationApi,
            "https://app.example.com",
            Duration.ofHours(1));
  }

  @Test
  void fullResetFlow_requestSendsEmail_thenResetChangesPassword() {
    Long userId = 5L;
    when(userApi.findActiveByEmail("john@example.com"))
        .thenReturn(Optional.of(new UserCredential(userId, "hash", List.of("EMPLOYEE"))));
    when(userApi.loadProfile(userId))
        .thenReturn(
            new UserResponse(
                userId,
                "John",
                "Doe",
                "john@example.com",
                false,
                new UserPreferencesResponse("light", "en"),
                null,
                null));

    boolean requested = passwordResetService.requestReset("john@example.com");

    assertThat(requested).isTrue();

    ArgumentCaptor<PasswordResetToken> tokenCaptor =
        ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(tokenRepository).deleteByUserId(userId);
    verify(tokenRepository).save(tokenCaptor.capture());

    PasswordResetToken savedToken = tokenCaptor.getValue();
    assertThat(savedToken.getUserId()).isEqualTo(userId);
    assertThat(savedToken.getExpiresAt()).isAfter(OffsetDateTime.now());

    ArgumentCaptor<NotificationRecipient> recipientCaptor =
        ArgumentCaptor.forClass(NotificationRecipient.class);
    ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
    verify(notificationApi).sendPasswordResetEmail(recipientCaptor.capture(), linkCaptor.capture());

    assertThat(recipientCaptor.getValue().email()).isEqualTo("john@example.com");
    assertThat(linkCaptor.getValue()).startsWith("https://app.example.com/reset-password?token=");

    // Phase 2: validate + consume the token to change the password
    String tokenHash = savedToken.getTokenHash();
    PasswordResetToken storedToken =
        new PasswordResetToken(userId, tokenHash, savedToken.getExpiresAt());

    when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedToken));

    String rawToken =
        linkCaptor.getValue().replace("https://app.example.com/reset-password?token=", "");
    boolean reset = passwordResetService.reset(rawToken, "newSecurePass123");

    assertThat(reset).isTrue();
    verify(userApi).resetPassword(userId, "newSecurePass123");
    assertThat(storedToken.getUsedAt()).isNotNull();
  }

  @Test
  void requestResetForNonExistentEmail_returnsFalseAndNoEmailSent() {
    when(userApi.findActiveByEmail("nobody@example.com")).thenReturn(Optional.empty());

    boolean requested = passwordResetService.requestReset("nobody@example.com");

    assertThat(requested).isFalse();
    verify(notificationApi, org.mockito.Mockito.never())
        .sendPasswordResetEmail(any(NotificationRecipient.class), any(String.class));
  }

  @Test
  void resetWithExpiredTokenFails() {
    PasswordResetToken expiredToken =
        new PasswordResetToken(5L, "somehash", OffsetDateTime.now().minusHours(2));
    when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expiredToken));

    boolean reset = passwordResetService.reset("any-raw-token", "newPassword");

    assertThat(reset).isFalse();
    verify(userApi, org.mockito.Mockito.never()).resetPassword(any(), any());
  }

  @Test
  void resetWithAlreadyUsedTokenFails() {
    PasswordResetToken usedToken =
        new PasswordResetToken(5L, "somehash", OffsetDateTime.now().plusHours(1));
    usedToken.setUsedAt(OffsetDateTime.now().minusMinutes(10));
    when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(usedToken));

    boolean reset = passwordResetService.reset("any-raw-token", "newPassword");

    assertThat(reset).isFalse();
    verify(userApi, org.mockito.Mockito.never()).resetPassword(any(), any());
  }
}
