package com.oman.EmpPulse.config;

import com.oman.EmpPulse.entity.Admin;
import com.oman.EmpPulse.entity.User;
import com.oman.EmpPulse.entity.UserLanguage;
import com.oman.EmpPulse.entity.UserTheme;
import com.oman.EmpPulse.repository.AdminRepository;
import com.oman.EmpPulse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements CommandLineRunner {

  private final UserRepository userRepository;
  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${APP_OWNER_EMAIL}")
  private String ownerEmail;

  @Value("${APP_OWNER_PASSWORD}")
  private String ownerPassword;

  public AppStartupRunner(
      UserRepository userRepository,
      AdminRepository adminRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.adminRepository = adminRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    seedUser("System", "Owner", ownerEmail, ownerPassword, true, false);
  }

  private void seedUser(
      String name,
      String surname,
      String email,
      String password,
      boolean isOwner,
      boolean createAdminEntity) {
    if (userRepository.findByEmail(email).isPresent()) return;
    User user = new User();
    user.setName(name);
    user.setSurname(surname);
    user.setEmail(email);
    user.setPassHash(passwordEncoder.encode(password));
    user.setTheme(UserTheme.LIGHT);
    user.setLanguage(UserLanguage.ENG);
    user.setOwner(isOwner);
    userRepository.save(user);
    if (createAdminEntity) {
      adminRepository.save(new Admin(user.getId()));
    }
    System.out.println("Seeded owner: " + email);
  }
}
