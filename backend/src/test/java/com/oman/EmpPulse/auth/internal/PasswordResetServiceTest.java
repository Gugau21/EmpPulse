package com.oman.EmpPulse.auth.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.UserCredential;
import com.oman.EmpPulse.user.api.UserPreferencesResponse;
import com.oman.EmpPulse.user.api.UserResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

  @Mock private PasswordResetTokenRepository tokenRepository;
  @Mock private UserApi userApi;
  @Mock private NotificationApi notificationApi;

  private PasswordResetService passwordResetService;

  @BeforeEach
  void setUp() {
    passwordResetService =
        new PasswordResetService(
            tokenRepository, userApi, notificationApi, "http://localhost", Duration.ofMinutes(15));
  }

  @Test
  void requestResetReturnsFalseWhenUserNotFound() {
    when(userApi.findActiveByEmail("missing@x.com")).thenReturn(Optional.empty());

    boolean created = passwordResetService.requestReset("missing@x.com");

    assertThat(created).isFalse();
  }

  @Test
  void requestResetDeletesOldTokenSavesNewTokenAndSendsEmail() {
    when(userApi.findActiveByEmail("user@x.com"))
        .thenReturn(Optional.of(new UserCredential(10L, "hash", List.of("EMPLOYEE"))));
    when(userApi.loadProfile(10L))
        .thenReturn(
            new UserResponse(
                10L,
                "Emp",
                "One",
                "user@x.com",
                false,
                new UserPreferencesResponse("light", "en"),
                null,
                null));

    boolean created = passwordResetService.requestReset("user@x.com");

    assertThat(created).isTrue();
    verify(tokenRepository).deleteByUserId(10L);
    verify(tokenRepository).save(any(PasswordResetToken.class));
    ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
    verify(notificationApi).sendPasswordResetEmail(any(), linkCaptor.capture());
    assertThat(linkCaptor.getValue()).startsWith("http://localhost/reset-password?token=");
  }

  @Test
  void validateAndConsumeRejectsExpiredOrUsedToken() {
    String rawToken = "token-123";
    PasswordResetToken expired =
        new PasswordResetToken(10L, sha256Hex(rawToken), OffsetDateTime.now().minusMinutes(1));
    when(tokenRepository.findByTokenHash(sha256Hex(rawToken))).thenReturn(Optional.of(expired));

    assertThat(passwordResetService.validateAndConsume(rawToken)).isEmpty();

    PasswordResetToken used =
        new PasswordResetToken(10L, sha256Hex(rawToken), OffsetDateTime.now().plusMinutes(5));
    used.setUsedAt(OffsetDateTime.now());
    when(tokenRepository.findByTokenHash(sha256Hex(rawToken))).thenReturn(Optional.of(used));

    assertThat(passwordResetService.validateAndConsume(rawToken)).isEmpty();
  }

  @Test
  void resetReturnsFalseForInvalidTokenAndTrueForValidToken() {
    when(tokenRepository.findByTokenHash(sha256Hex("invalid"))).thenReturn(Optional.empty());

    assertThat(passwordResetService.reset("invalid", "new-password")).isFalse();

    PasswordResetToken valid =
        new PasswordResetToken(10L, sha256Hex("valid"), OffsetDateTime.now().plusMinutes(10));
    when(tokenRepository.findByTokenHash(sha256Hex("valid"))).thenReturn(Optional.of(valid));

    assertThat(passwordResetService.reset("valid", "new-password")).isTrue();
    verify(userApi).resetPassword(10L, "new-password");
  }

  private String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
