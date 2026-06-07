package com.oman.EmpPulse.bootstrap;

import com.oman.EmpPulse.user.api.UserDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OwnerSeeder implements CommandLineRunner {

  private final UserDirectory userDirectory;

  @Value("${APP_OWNER_EMAIL}")
  private String ownerEmail;

  @Value("${APP_OWNER_PASSWORD}")
  private String ownerPassword;

  public OwnerSeeder(UserDirectory userDirectory) {
    this.userDirectory = userDirectory;
  }

  @Override
  public void run(String... args) {
    userDirectory.ensureOwnerExists(ownerEmail, ownerPassword);
  }
}
