package com.oman.EmpPulse.defaulthours.web;

import com.oman.EmpPulse.defaulthours.dto.DefaultWeekHoursRequest;
import com.oman.EmpPulse.defaulthours.internal.DefaultHoursService;
import com.oman.EmpPulse.shared.security.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentDefaultHoursController {

  private final DefaultHoursService defaultHoursService;

  public DepartmentDefaultHoursController(DefaultHoursService defaultHoursService) {
    this.defaultHoursService = defaultHoursService;
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{departmentId}/default-hours")
  public ResponseEntity<?> setDefaultHours(
      @PathVariable Long departmentId,
      @RequestBody DefaultWeekHoursRequest req,
      Authentication authentication) {
    return ResponseEntity.ok(
        defaultHoursService.setDepartmentDefaultHours(
            departmentId, req, AuthUtils.getUserId(authentication)));
  }
}
