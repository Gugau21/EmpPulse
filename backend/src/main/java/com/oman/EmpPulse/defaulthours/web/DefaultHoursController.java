package com.oman.EmpPulse.defaulthours.web;

import com.oman.EmpPulse.defaulthours.dto.DefaultWeekHoursRequest;
import com.oman.EmpPulse.defaulthours.internal.DefaultHoursService;
import com.oman.EmpPulse.shared.security.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class DefaultHoursController {

  private final DefaultHoursService defaultHoursService;

  public DefaultHoursController(DefaultHoursService defaultHoursService) {
    this.defaultHoursService = defaultHoursService;
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{employeeId}/default-hours")
  public ResponseEntity<?> setDefaultHours(
      @PathVariable Long employeeId,
      @RequestBody DefaultWeekHoursRequest req,
      Authentication authentication) {
    return ResponseEntity.ok(
        defaultHoursService.setEmployeeDefaultHours(
            employeeId, req, AuthUtils.getUserId(authentication)));
  }
}
