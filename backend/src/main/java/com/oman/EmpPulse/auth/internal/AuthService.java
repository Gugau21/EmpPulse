package com.oman.EmpPulse.auth.internal;

import com.oman.EmpPulse.user.api.UserCredential;
import com.oman.EmpPulse.user.api.UserDirectory;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserDirectory userDirectory;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserDirectory userDirectory, PasswordEncoder passwordEncoder) {
    this.userDirectory = userDirectory;
    this.passwordEncoder = passwordEncoder;
  }

  public Optional<UserCredential> authenticate(String email, String password) {
    return userDirectory
        .findActiveByEmail(email)
        .filter(credential -> passwordEncoder.matches(password, credential.passwordHash()));
  }
}
