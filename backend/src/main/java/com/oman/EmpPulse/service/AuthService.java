package com.oman.EmpPulse.service;

import com.oman.EmpPulse.entity.User;
import com.oman.EmpPulse.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Optional<User> authenticate(String email, String password) {
    Optional<User> userOpt = userRepository.findByEmailAndIsDeletedFalse(email);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (passwordEncoder.matches(password, user.getPassHash())) {
        return Optional.of(user);
      }
    }
    return Optional.empty();
  }
}
