package com.oman.EmpPulse.department.web;

import com.oman.EmpPulse.department.dto.DepartmentCreateRequest;
import com.oman.EmpPulse.department.dto.DepartmentUpdateRequest;
import com.oman.EmpPulse.department.internal.DepartmentService;
import com.oman.EmpPulse.shared.security.AuthUtils;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

  private final DepartmentService departmentService;

  public DepartmentController(DepartmentService departmentService) {
    this.departmentService = departmentService;
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping
  public ResponseEntity<?> listDepartments(Authentication authentication) {
    return ResponseEntity.ok(
        departmentService.getDepartmentsForAdmin(AuthUtils.getUserId(authentication)));
  }

  @PreAuthorize("hasAuthority('OWNER')")
  @PostMapping
  public ResponseEntity<?> createDepartment(@RequestBody DepartmentCreateRequest req) {
    departmentService.createDepartment(req);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/{departmentId}")
  public ResponseEntity<?> getDepartment(
      @PathVariable Long departmentId, Authentication authentication) {
    boolean isAdminOfDepartment =
        departmentService.isAdminOfDepartment(AuthUtils.getUserId(authentication), departmentId);

    if (isAdminOfDepartment) {
      return ResponseEntity.ok(departmentService.getDepartment(departmentId));
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("code", "FORBIDDEN", "message", "No access to this department"));
  }

  @PreAuthorize("hasAuthority('OWNER')")
  @DeleteMapping("/{departmentId}")
  public ResponseEntity<?> deleteDepartment(@PathVariable Long departmentId) {
    departmentService.deleteDepartment(departmentId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('OWNER')")
  @PatchMapping("/{departmentId}")
  public ResponseEntity<?> updateDepartment(
      @PathVariable Long departmentId, @RequestBody DepartmentUpdateRequest req) {
    departmentService.updateDepartment(departmentId, req);
    return ResponseEntity.noContent().build();
  }
}
