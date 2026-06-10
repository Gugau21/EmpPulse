package com.oman.EmpPulse.bootstrap;

import com.oman.EmpPulse.user.api.UserApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OwnerSeeder implements CommandLineRunner {

  private final UserApi userApi;

  @Value("${APP_OWNER_EMAIL}")
  private String ownerEmail;

  @Value("${APP_OWNER_PASSWORD}")
  private String ownerPassword;

  public OwnerSeeder(UserApi userApi) {
    this.userApi = userApi;
  }

  @Override
  public void run(String... args) {
    userApi.ensureOwnerExists(ownerEmail, ownerPassword);
  }
}
