package com.oman.EmpPulse.controller;

import com.oman.EmpPulse.dto.UserCreateRequest;
import com.oman.EmpPulse.security.AuthUtils;
import com.oman.EmpPulse.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        return ResponseEntity.ok(userService.buildMeResponse(AuthUtils.getUserId(authentication)));
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PostMapping("/api/users")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest req, Authentication authentication) {
        userService.createUser(req, AuthUtils.getRole(authentication));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @DeleteMapping("/api/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.softDeleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
