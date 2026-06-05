package com.oman.EmpPulse.controller;

import com.oman.EmpPulse.security.AuthUtils;
import com.oman.EmpPulse.service.EmployeeService;
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
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    public ResponseEntity<?> listEmployees(Authentication authentication) {
        if (AuthUtils.isOwner(authentication)) {
            return ResponseEntity.ok(employeeService.getAllEmployees());
        }
        return ResponseEntity.ok(employeeService.getEmployeesForAdmin(AuthUtils.getUserId(authentication)));
    }
}
