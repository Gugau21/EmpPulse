package com.oman.EmpPulse.controller;

import com.oman.EmpPulse.dto.LoginRequest;
import com.oman.EmpPulse.entity.User;
import com.oman.EmpPulse.service.AuthService;
import com.oman.EmpPulse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthService authService, UserService userService,
                          SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletRequest request, HttpServletResponse response) {
        Optional<User> userOpt = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", "INVALID_CREDENTIALS", "message", "Invalid credentials"));
        }

        User user = userOpt.get();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getId(), null,
                List.of(new SimpleGrantedAuthority(user.getRole().name())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        HttpSession session = request.getSession(true);
        session.setAttribute("USER_THEME", user.getTheme());
        session.setAttribute("USER_LANGUAGE", user.getLanguage());

        return ResponseEntity.ok(userService.buildMeResponse(user.getId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok("Logout successful");
    }
}
