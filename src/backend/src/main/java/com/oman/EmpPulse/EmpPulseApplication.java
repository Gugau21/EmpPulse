package com.oman.EmpPulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmpPulseApplication {

  public static void main(String[] args) {
    SpringApplication.run(EmpPulseApplication.class, args);
  }
}
