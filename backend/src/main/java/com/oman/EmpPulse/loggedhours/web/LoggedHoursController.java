package com.oman.EmpPulse.loggedhours.web;

import com.oman.EmpPulse.loggedhours.dto.LoggedHoursCreateRequest;
import com.oman.EmpPulse.loggedhours.dto.LoggedHoursUpdateRequest;
import com.oman.EmpPulse.loggedhours.internal.LoggedHoursService;
import com.oman.EmpPulse.shared.security.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class LoggedHoursController {

  private final LoggedHoursService loggedHoursService;

  public LoggedHoursController(LoggedHoursService loggedHoursService) {
    this.loggedHoursService = loggedHoursService;
  }

  @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
  @GetMapping("/{employeeId}/logged-hours")
  public ResponseEntity<?> listLoggedHours(
      @PathVariable Long employeeId, Authentication authentication) {
    return ResponseEntity.ok(
        loggedHoursService.listLoggedHours(
            employeeId, AuthUtils.getUserId(authentication), AuthUtils.isAdmin(authentication)));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/{employeeId}/logged-hours")
  public ResponseEntity<?> createLoggedHours(
      @PathVariable Long employeeId,
      @RequestBody LoggedHoursCreateRequest req,
      Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            loggedHoursService.createLoggedHours(
                employeeId, req, AuthUtils.getUserId(authentication)));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/{employeeId}/logged-hours/{loggedHoursId}")
  public ResponseEntity<?> editLoggedHours(
      @PathVariable Long employeeId,
      @PathVariable Long loggedHoursId,
      @RequestBody LoggedHoursUpdateRequest req,
      Authentication authentication) {
    return ResponseEntity.ok(
        loggedHoursService.updateLoggedHours(
            employeeId, loggedHoursId, req, AuthUtils.getUserId(authentication)));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/{employeeId}/logged-hours/{loggedHoursId}")
  public ResponseEntity<?> deleteLoggedHours(
      @PathVariable Long employeeId,
      @PathVariable Long loggedHoursId,
      Authentication authentication) {
    loggedHoursService.deleteLoggedHours(
        employeeId, loggedHoursId, AuthUtils.getUserId(authentication));
    return ResponseEntity.noContent().build();
  }
}
