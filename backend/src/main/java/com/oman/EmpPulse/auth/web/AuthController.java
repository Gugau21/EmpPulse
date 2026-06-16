package com.oman.EmpPulse.auth.web;

import com.oman.EmpPulse.auth.dto.LoginRequest;
import com.oman.EmpPulse.auth.dto.PasswordForgotRequest;
import com.oman.EmpPulse.auth.internal.AuthService;
import com.oman.EmpPulse.auth.internal.PasswordResetService;
import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.UserCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final PasswordResetService passwordResetService;
  private final UserApi userApi;
  private final SecurityContextRepository securityContextRepository;

  public AuthController(
      AuthService authService,
      PasswordResetService passwordResetService,
      UserApi userApi,
      SecurityContextRepository securityContextRepository) {
    this.authService = authService;
    this.passwordResetService = passwordResetService;
    this.userApi = userApi;
    this.securityContextRepository = securityContextRepository;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestBody LoginRequest loginRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    Optional<UserCredential> credentialOpt =
        authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
    if (credentialOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("code", "INVALID_CREDENTIALS", "message", "Invalid credentials"));
    }

    UserCredential credential = credentialOpt.get();

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            credential.id(),
            null,
            credential.authorities().stream().map(SimpleGrantedAuthority::new).toList());
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
    securityContextRepository.saveContext(context, request, response);

    return ResponseEntity.ok(userApi.loadProfile(credential.id()));
  }

  @PostMapping("/password/forgot")
  public ResponseEntity<?> forgotPassword(@RequestBody PasswordForgotRequest request) {
    if (!StringUtils.hasText(request.getEmail())) {
      return ResponseEntity.badRequest()
          .body(Map.of("code", "EMAIL_REQUIRED", "message", "Email is required"));
    }
    if (!passwordResetService.requestReset(request.getEmail())) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("code", "USER_NOT_FOUND", "message", "No account found for that email"));
    }
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    SecurityContextHolder.clearContext();
    HttpSession session = request.getSession(false);
    if (session != null) session.invalidate();
    return ResponseEntity.ok("Logout successful");
  }
}
