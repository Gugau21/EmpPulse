package com.oman.EmpPulse.auth.web;

import com.oman.EmpPulse.auth.dto.LoginRequest;
import com.oman.EmpPulse.auth.internal.AuthService;
import com.oman.EmpPulse.user.api.UserCredential;
import com.oman.EmpPulse.user.api.UserDirectory;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final UserDirectory userDirectory;
  private final SecurityContextRepository securityContextRepository;

  public AuthController(
      AuthService authService,
      UserDirectory userDirectory,
      SecurityContextRepository securityContextRepository) {
    this.authService = authService;
    this.userDirectory = userDirectory;
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

    return ResponseEntity.ok(userDirectory.loadProfile(credential.id()));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    SecurityContextHolder.clearContext();
    HttpSession session = request.getSession(false);
    if (session != null) session.invalidate();
    return ResponseEntity.ok("Logout successful");
  }
}
