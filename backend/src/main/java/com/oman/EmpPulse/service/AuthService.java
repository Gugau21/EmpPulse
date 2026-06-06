package com.oman.EmpPulse.service;

import com.oman.EmpPulse.entity.User;
import com.oman.EmpPulse.repository.AdminRepository;
import com.oman.EmpPulse.repository.EmployeeRepository;
import com.oman.EmpPulse.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final AdminRepository adminRepository;
  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(
      UserRepository userRepository,
      AdminRepository adminRepository,
      EmployeeRepository employeeRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.adminRepository = adminRepository;
    this.employeeRepository = employeeRepository;
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

  public List<SimpleGrantedAuthority> getAuthorities(User user) {
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    if (user.isOwner()) authorities.add(new SimpleGrantedAuthority("OWNER"));
    if (adminRepository.findByUserId(user.getId()).isPresent())
      authorities.add(new SimpleGrantedAuthority("ADMIN"));
    if (employeeRepository.findByUserId(user.getId()).isPresent())
      authorities.add(new SimpleGrantedAuthority("EMPLOYEE"));
    return authorities;
  }
}
