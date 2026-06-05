package com.oman.EmpPulse.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                .ignoringRequestMatchers("/api/auth/login"))
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .securityContext(sc -> sc.securityContextRepository(securityContextRepository()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/assets/**",
                "/*.js", "/*.css", "/*.svg", "/*.ico",
                "/api/auth/login", "/api/auth/logout", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    objectMapper.writeValue(
                        response.getWriter(),
                        Map.of("code", "UNAUTHORIZED", "message", "Unauthorized"));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    objectMapper.writeValue(
                        response.getWriter(),
                        Map.of("code", "FORBIDDEN", "message", "Forbidden"));
                })
            );

        return http.build();
    }
}
