package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.AdminSummaryResponse;
import com.oman.EmpPulse.user.api.UserSummaryResponse;
import com.oman.EmpPulse.user.dto.AdminListResponse;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService implements AdminApi {

  private final AdminRepository adminRepository;
  private final UserRepository userRepository;

  public AdminService(AdminRepository adminRepository, UserRepository userRepository) {
    this.adminRepository = adminRepository;
    this.userRepository = userRepository;
  }

  @Override
  public List<Admin> findAllByIds(Collection<Long> ids) {
    return adminRepository.findAllById(ids);
  }

  @Override
  public Optional<Admin> findById(Long id) {
    return adminRepository.findById(id);
  }

  @Override
  public AdminSummaryResponse toAdminSummaryResponse(Admin admin) {
    User user =
        userRepository
            .findById(admin.getId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));
    List<Long> deptIds = admin.getDepartments().stream().map(Department::getId).toList();

    return new AdminSummaryResponse(
        admin.getId(),
        new UserSummaryResponse(user.getId(), user.getName(), user.getSurname(), user.getEmail()),
        deptIds);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> departmentIdsForAdminUser(Long adminId) {
    Admin admin =
        adminRepository
            .findById(adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
    return admin.getDepartments().stream().map(Department::getId).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean overseesDepartment(Long adminId, Long departmentId) {
    return adminRepository
        .findById(adminId)
        .map(admin -> admin.getDepartments().stream().anyMatch(d -> d.getId().equals(departmentId)))
        .orElse(false);
  }

  @Transactional(readOnly = true)
  public AdminListResponse getAllAdmins() {
    List<AdminSummaryResponse> items =
        adminRepository.findByActiveTrue().stream().map(this::toAdminSummaryResponse).toList();
    return new AdminListResponse(items);
  }
}
