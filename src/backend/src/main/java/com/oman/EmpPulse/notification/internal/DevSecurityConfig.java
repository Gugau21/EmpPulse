package com.oman.EmpPulse.notification.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(name = "app.dev.test-email.enabled", havingValue = "true")
public class DevSecurityConfig {

  @Bean
  @Order(0)
  public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/dev/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable());
    return http.build();
  }
}
