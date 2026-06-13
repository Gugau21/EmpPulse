package com.oman.EmpPulse.user.web;

import com.oman.EmpPulse.shared.security.AuthUtils;
import com.oman.EmpPulse.user.dto.BonusVacationDayRequest;
import com.oman.EmpPulse.user.dto.UserCreateRequest;
import com.oman.EmpPulse.user.dto.UserUpdateRequest;
import com.oman.EmpPulse.user.internal.UserService;
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
    return ResponseEntity.ok(userService.loadProfile(AuthUtils.getUserId(authentication)));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/api/users")
  public ResponseEntity<?> createUser(
      @RequestBody UserCreateRequest req, Authentication authentication) {
    userService.createUser(
        req, AuthUtils.getUserId(authentication), AuthUtils.isOwner(authentication));
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/api/users/{userId}")
  public ResponseEntity<?> getUserProfile(
      @PathVariable Long userId, Authentication authentication) {
    return ResponseEntity.ok(
        userService.getUserProfile(
            userId, AuthUtils.getUserId(authentication), AuthUtils.isOwner(authentication)));
  }

  @PreAuthorize("hasAuthority('OWNER')")
  @DeleteMapping("/api/users/{userId}")
  public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
    userService.softDeleteUser(userId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/api/users/{userId}")
  public ResponseEntity<?> updateUser(
      @PathVariable Long userId,
      @RequestBody UserUpdateRequest req,
      Authentication authentication) {
    userService.updateUser(
        userId, req, AuthUtils.getUserId(authentication), AuthUtils.isOwner(authentication));
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/api/users/{userId}/bonus-vacation-days")
  public ResponseEntity<?> updateBonusVacationDays(
      @PathVariable Long userId,
      @RequestBody BonusVacationDayRequest req,
      Authentication authentication) {
    userService.updateBonusVacationDays(userId, req, AuthUtils.getUserId(authentication));
    return ResponseEntity.noContent().build();
  }
}
