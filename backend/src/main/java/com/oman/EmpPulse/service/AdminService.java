package com.oman.EmpPulse.service;

import com.oman.EmpPulse.dto.response.AdminListResponse;
import com.oman.EmpPulse.dto.response.AdminSummaryResponse;
import com.oman.EmpPulse.repository.AdminRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

  private final AdminRepository adminRepository;
  private final ServiceUtils serviceUtils;

  public AdminService(AdminRepository adminRepository, ServiceUtils serviceUtils) {
    this.adminRepository = adminRepository;
    this.serviceUtils = serviceUtils;
  }

  @Transactional(readOnly = true)
  public AdminListResponse getAllAdmins() {
    List<AdminSummaryResponse> items =
        adminRepository.findAll().stream()
            .map(serviceUtils::toAdminSummaryResponse)
            .filter(Objects::nonNull)
            .toList();
    return new AdminListResponse(items);
  }
}
