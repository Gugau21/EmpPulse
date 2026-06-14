package com.oman.EmpPulse.leave.web;

import com.oman.EmpPulse.leave.dto.LeaveCreateRequest;
import com.oman.EmpPulse.leave.dto.LeaveModificationRequest;
import com.oman.EmpPulse.leave.dto.LeaveResponseRequest;
import com.oman.EmpPulse.leave.dto.LeaveUpdateRequest;
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

  @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
  @GetMapping
  public ResponseEntity<?> listLeaveRequests(Authentication authentication) {
    return ResponseEntity.ok(
        leaveService.listLeaveRequests(
            AuthUtils.getUserId(authentication), AuthUtils.isAdmin(authentication)));
  }

  @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
  @GetMapping("/{leaveRequestId}")
  public ResponseEntity<?> getLeaveRequest(
      @PathVariable Long leaveRequestId, Authentication authentication) {
    return ResponseEntity.ok(
        leaveService.getLeaveRequest(leaveRequestId, AuthUtils.getUserId(authentication)));
  }

  @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
  @PatchMapping("/{leaveRequestId}")
  public ResponseEntity<?> updateLeaveRequest(
      @PathVariable Long leaveRequestId,
      @RequestBody LeaveUpdateRequest req,
      Authentication authentication) {
    return ResponseEntity.ok(
        leaveService.updateLeaveRequest(
            leaveRequestId,
            req,
            AuthUtils.getUserId(authentication),
            AuthUtils.isAdmin(authentication)));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/{leaveRequestId}/response")
  public ResponseEntity<?> respondToLeaveRequest(
      @PathVariable Long leaveRequestId,
      @RequestBody LeaveResponseRequest req,
      Authentication authentication) {
    return ResponseEntity.ok(
        leaveService.respondToLeaveRequest(
            leaveRequestId, req, AuthUtils.getUserId(authentication)));
  }

  @PreAuthorize("hasAuthority('EMPLOYEE')")
  @PostMapping("/{leaveRequestId}/modifications")
  public ResponseEntity<?> createModification(
      @PathVariable Long leaveRequestId,
      @RequestBody LeaveModificationRequest req,
      Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            leaveService.createModification(
                leaveRequestId, req, AuthUtils.getUserId(authentication)));
  }

  @PreAuthorize("hasAuthority('EMPLOYEE')")
  @PatchMapping("/{leaveRequestId}/cancel")
  public ResponseEntity<?> cancelLeaveRequest(
      @PathVariable Long leaveRequestId, Authentication authentication) {
    return ResponseEntity.ok(
        leaveService.cancelLeaveRequest(leaveRequestId, AuthUtils.getUserId(authentication)));
  }

  @PreAuthorize("hasAuthority('EMPLOYEE')")
  @DeleteMapping("/{leaveRequestId}")
  public ResponseEntity<?> deleteLeaveRequest(
      @PathVariable Long leaveRequestId, Authentication authentication) {
    leaveService.deleteLeaveRequest(leaveRequestId, AuthUtils.getUserId(authentication));
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
  @PostMapping
  public ResponseEntity<?> createLeaveRequest(
      @RequestBody LeaveCreateRequest req, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            leaveService.createLeaveRequest(
                req, AuthUtils.getUserId(authentication), AuthUtils.isAdmin(authentication)));
  }
}
