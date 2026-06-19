package com.oman.EmpPulse.user.web;

import com.oman.EmpPulse.shared.security.AuthUtils;
import com.oman.EmpPulse.user.internal.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

  private final EmployeeService employeeService;

  public EmployeeController(EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<?> listEmployees(Authentication authentication) {
    return ResponseEntity.ok(
        employeeService.getEmployeesForAdmin(AuthUtils.getUserId(authentication)));
  }
}
