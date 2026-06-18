package com.oman.EmpPulse.auth.internal;

import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.UserCredential;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserApi userApi;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserApi userApi, PasswordEncoder passwordEncoder) {
    this.userApi = userApi;
    this.passwordEncoder = passwordEncoder;
  }

  public Optional<UserCredential> authenticate(String email, String password) {
    return userApi
        .findActiveByEmail(email)
        .filter(credential -> passwordEncoder.matches(password, credential.passwordHash()));
  }
}
