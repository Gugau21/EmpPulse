package com.oman.EmpPulse.auth.internal;

import com.oman.EmpPulse.user.api.UserApi;
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

/**
 * Issues and validates single-use, time-limited password reset tokens. The raw token only ever
 * leaves the application inside the emailed reset link; the database stores nothing but its SHA-256
 * hash, so a leaked table cannot be used to reset passwords.
 *
 * <p>No controllers are wired yet: {@link #createResetLink(String)} produces the link that the
 * future {@code /api/auth/password/forgot} endpoint will pass to {@code
 * NotificationApi.sendPasswordResetEmail}, and {@link #validateAndConsume(String)} backs the future
 * {@code /api/auth/password/reset} endpoint.
 */
@Service
public class PasswordResetService {

  private static final int TOKEN_BYTES = 32;

  private final PasswordResetTokenRepository tokenRepository;
  private final UserApi userApi;
  private final String appBaseUrl;
  private final Duration tokenTtl;
  private final SecureRandom secureRandom = new SecureRandom();

  public PasswordResetService(
      PasswordResetTokenRepository tokenRepository,
      UserApi userApi,
      @Value("${app.base-url}") String appBaseUrl,
      @Value("${app.password-reset.token-ttl}") Duration tokenTtl) {
    this.tokenRepository = tokenRepository;
    this.userApi = userApi;
    this.appBaseUrl = appBaseUrl;
    this.tokenTtl = tokenTtl;
  }

  /**
   * Creates a fresh reset link for the user with the given email, invalidating any previous link.
   *
   * @param email the address that requested a reset
   * @return the full reset URL, or empty if no active user has that email (so callers can respond
   *     identically whether or not the account exists)
   */
  @Transactional
  public Optional<String> createResetLink(String email) {
    return userApi
        .findActiveByEmail(email)
        .map(
            credential -> {
              tokenRepository.deleteByUserId(credential.id());

              String rawToken = generateRawToken();
              tokenRepository.save(
                  new PasswordResetToken(
                      credential.id(), sha256Hex(rawToken), OffsetDateTime.now().plus(tokenTtl)));

              return appBaseUrl + "/reset?token=" + rawToken;
            });
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
