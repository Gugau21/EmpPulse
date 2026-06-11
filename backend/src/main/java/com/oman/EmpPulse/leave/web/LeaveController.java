package com.oman.EmpPulse.leave.web;

import com.oman.EmpPulse.leave.dto.LeaveCreateRequest;
import com.oman.EmpPulse.leave.internal.LeaveService;
import com.oman.EmpPulse.shared.security.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveController {

  private final LeaveService leaveService;

  public LeaveController(LeaveService leaveService) {
    this.leaveService = leaveService;
  }

  @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'EMPLOYEE')")
  @PostMapping
  public ResponseEntity<?> createLeaveRequest(
      @RequestBody LeaveCreateRequest req, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            leaveService.createLeaveRequest(
                req,
                AuthUtils.getUserId(authentication),
                AuthUtils.isAdmin(authentication),
                AuthUtils.isOwner(authentication)));
  }
}
