package com.oman.EmpPulse.service;

import com.oman.EmpPulse.dto.response.AdminListResponse;
import com.oman.EmpPulse.dto.response.AdminSummaryResponse;
import com.oman.EmpPulse.dto.response.UserSummaryResponse;
import com.oman.EmpPulse.entity.Admin;
import com.oman.EmpPulse.entity.AdminDepartment;
import com.oman.EmpPulse.entity.User;
import com.oman.EmpPulse.repository.AdminDepartmentRepository;
import com.oman.EmpPulse.repository.AdminRepository;
import com.oman.EmpPulse.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final AdminDepartmentRepository adminDepartmentRepository;
    private final UserRepository userRepository;

    public AdminService(AdminRepository adminRepository,
                        AdminDepartmentRepository adminDepartmentRepository,
                        UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.adminDepartmentRepository = adminDepartmentRepository;
        this.userRepository = userRepository;
    }

    public AdminListResponse getAllAdmins() {
        List<AdminSummaryResponse> items = adminRepository.findAll().stream()
                .map(this::toAdminSummaryResponse)
                .filter(java.util.Objects::nonNull)
                .toList();
        return new AdminListResponse(items);
    }

    /** Returns null when the admin's user has been soft-deleted, so callers can filter it out. */
    private AdminSummaryResponse toAdminSummaryResponse(Admin admin) {
        User user = userRepository.findById(admin.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));
        if (user.isDeleted()) {
            return null;
        }
        List<Long> deptIds = adminDepartmentRepository.findByAdminId(admin.getId())
                .stream().map(AdminDepartment::getDepartmentId).toList();
        return new AdminSummaryResponse(admin.getId(),
                new UserSummaryResponse(user.getId(), user.getName(), user.getSurname(), user.getEmail()),
                deptIds);
    }
}
