package com.oman.EmpPulse.auth.internal;

import com.oman.EmpPulse.notification.api.NotificationApi;
import com.oman.EmpPulse.notification.api.NotificationRecipient;
import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.UserResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

  private static final int TOKEN_BYTES = 32;

  private final PasswordResetTokenRepository tokenRepository;
  private final UserApi userApi;
  private final NotificationApi notificationApi;
  private final String appBaseUrl;
  private final Duration tokenTtl;
  private final SecureRandom secureRandom = new SecureRandom();

  public PasswordResetService(
      PasswordResetTokenRepository tokenRepository,
      UserApi userApi,
      NotificationApi notificationApi,
      @Value("${app.base-url}") String appBaseUrl,
      @Value("${app.password-reset.token-ttl}") Duration tokenTtl) {
    this.tokenRepository = tokenRepository;
    this.userApi = userApi;
    this.notificationApi = notificationApi;
    this.appBaseUrl = appBaseUrl;
    this.tokenTtl = tokenTtl;
  }

  /**
   * Issues a fresh reset link for the active user with the given email and emails it to them,
   * invalidating any previous link. The raw token never leaves this service except inside the
   * email.
   *
   * @param email the address that requested a reset
   * @return {@code true} if an active user has that email and a reset email was queued, {@code
   *     false} otherwise
   */
  @Transactional
  public boolean requestReset(String email) {
    return userApi
        .findActiveByEmail(email)
        .map(
            credential -> {
              tokenRepository.deleteByUserId(credential.id());

              String rawToken = generateRawToken();
              tokenRepository.save(
                  new PasswordResetToken(
                      credential.id(), sha256Hex(rawToken), OffsetDateTime.now().plus(tokenTtl)));

              String resetLink = appBaseUrl + "/reset?token=" + rawToken;
              UserResponse profile = userApi.loadProfile(credential.id());
              notificationApi.sendPasswordResetEmail(
                  new NotificationRecipient(profile.getEmail(), profile.getName()), resetLink);

              return true;
            })
        .orElse(false);
  }

  /**
   * Validates a raw reset token and marks it used. A token is valid only if it exists, has not been
   * used, and has not expired.
   *
   * @param rawToken the token taken from the reset link
   * @return the id of the user the token belongs to, or empty if the token is invalid
   */
  @Transactional
  public Optional<Long> validateAndConsume(String rawToken) {
    return tokenRepository
        .findByTokenHash(sha256Hex(rawToken))
        .filter(token -> token.getUsedAt() == null)
        .filter(token -> token.getExpiresAt().isAfter(OffsetDateTime.now()))
        .map(
            token -> {
              token.setUsedAt(OffsetDateTime.now());
              return token.getUserId();
            });
  }

  /**
   * Consumes a reset token and sets the user's new password, invalidating all of their sessions.
   * The caller is responsible for confirming the new-password fields match before calling this.
   *
   * @param rawToken the token taken from the reset link
   * @param newPassword the new password in plaintext
   * @return {@code true} if the token was valid and the password was changed, {@code false} if the
   *     token was invalid, expired, or already used
   */
  @Transactional
  public boolean reset(String rawToken, String newPassword) {
    Optional<Long> userId = validateAndConsume(rawToken);
    if (userId.isEmpty()) {
      return false;
    }
    userApi.resetPassword(userId.get(), newPassword);
    return true;
  }

  private String generateRawToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is required but unavailable", e);
    }
  }
}
