package com.oman.EmpPulse.controller;

import com.oman.EmpPulse.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping
    public ResponseEntity<?> listAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }
}
