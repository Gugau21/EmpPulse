package com.oman.EmpPulse.auth.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.UserCredential;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserApi userApi;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthService authService;

  @Test
  void authenticateReturnsCredentialForValidPassword() {
    UserCredential credential = new UserCredential(10L, "hash", List.of("EMPLOYEE"));
    when(userApi.findActiveByEmail("a@x.com")).thenReturn(Optional.of(credential));
    when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

    Optional<UserCredential> result = authService.authenticate("a@x.com", "secret");

    assertThat(result).contains(credential);
  }

  @Test
  void authenticateReturnsEmptyForWrongPassword() {
    UserCredential credential = new UserCredential(10L, "hash", List.of("EMPLOYEE"));
    when(userApi.findActiveByEmail("a@x.com")).thenReturn(Optional.of(credential));
    when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

    Optional<UserCredential> result = authService.authenticate("a@x.com", "wrong");

    assertThat(result).isEmpty();
  }

  @Test
  void authenticateReturnsEmptyForUnknownEmail() {
    when(userApi.findActiveByEmail("missing@x.com")).thenReturn(Optional.empty());

    Optional<UserCredential> result = authService.authenticate("missing@x.com", "secret");

    assertThat(result).isEmpty();
  }
}
