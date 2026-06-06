package com.oman.EmpPulse.service;

import com.oman.EmpPulse.dto.DepartmentCreateRequest;
import com.oman.EmpPulse.dto.DepartmentUpdateRequest;
import com.oman.EmpPulse.dto.response.*;
import com.oman.EmpPulse.entity.*;
import com.oman.EmpPulse.repository.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final AdminRepository adminRepository;
  private final EmployeeRepository employeeRepository;
  private final ServiceUtils serviceUtils;

  public DepartmentService(
      DepartmentRepository departmentRepository,
      AdminRepository adminRepository,
      EmployeeRepository employeeRepository,
      ServiceUtils serviceUtils) {
    this.departmentRepository = departmentRepository;
    this.adminRepository = adminRepository;
    this.employeeRepository = employeeRepository;
    this.serviceUtils = serviceUtils;
  }

  @Transactional(readOnly = true)
  public DepartmentResponse getDepartment(Long departmentId) {
    Department department =
        departmentRepository
            .findById(departmentId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    return toDepartmentResponse(department);
  }

  @Transactional(readOnly = true)
  public boolean isAdminOfDepartment(Long userId, Long departmentId) {
    return adminRepository
        .findByUserId(userId)
        .map(admin -> admin.getDepartments().stream().anyMatch(d -> d.getId().equals(departmentId)))
        .orElse(false);
  }

  @Transactional
  public void deleteDepartment(Long departmentId) {
    Department department =
        departmentRepository
            .findById(departmentId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    if (employeeRepository.existsByDepartmentId(departmentId)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Cannot delete department: employees are assigned");
    }
    if (!department.getAdmins().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Cannot delete department: administrators are assigned");
    }
    departmentRepository.delete(department);
  }

  @Transactional(readOnly = true)
  public DepartmentListResponse getAllDepartments() {
    List<DepartmentResponse> items =
        departmentRepository.findAll().stream().map(this::toDepartmentResponse).toList();
    return new DepartmentListResponse(items);
  }

  @Transactional(readOnly = true)
  public DepartmentListResponse getDepartmentsForAdmin(Long userId) {
    List<Long> deptIds = serviceUtils.getDeptIdsForAdminUser(userId);
    List<DepartmentResponse> items =
        departmentRepository.findAllById(deptIds).stream().map(this::toDepartmentResponse).toList();
    return new DepartmentListResponse(items);
  }

  @Transactional
  public void createDepartment(DepartmentCreateRequest req) {
    if (departmentRepository.existsByName(req.getName())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already in use");
    }

    Department department = new Department(req.getName());

    if (req.getAdminIds() != null) {
      department.setAdmins(loadAdmins(req.getAdminIds()));
    }

    departmentRepository.save(department);
  }

  @Transactional
  public void updateDepartment(Long departmentId, DepartmentUpdateRequest req) {
    Department department =
        departmentRepository
            .findById(departmentId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

    if (req.getName() != null) {
      validateNameAvailable(req.getName(), department.getName());
      department.setName(req.getName());
    }

    if (req.getAdminIds() != null) {
      validateAdminDetach(department.getAdmins(), req.getAdminIds());
      department.setAdmins(loadAdmins(req.getAdminIds()));
    }
  }

  private void validateNameAvailable(String newName, String currentName) {
    if (!newName.equals(currentName) && departmentRepository.existsByName(newName)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already in use");
    }
  }

  private void validateAdminDetach(Set<Admin> currentAdmins, List<Long> newAdminIds) {
    Set<Long> newAdminIdSet = new HashSet<>(newAdminIds);
    for (Admin admin : currentAdmins) {
      if (!newAdminIdSet.contains(admin.getId()) && admin.getDepartments().size() <= 1) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Cannot detach admin " + admin.getId() + ": must oversee more than 1 department");
      }
    }
  }

  private Set<Admin> loadAdmins(List<Long> adminIds) {
    List<Admin> admins = adminRepository.findAllById(adminIds);
    if (admins.size() != new HashSet<>(adminIds).size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found");
    }
    return new HashSet<>(admins);
  }

  private DepartmentResponse toDepartmentResponse(Department department) {
    List<AdminSummaryResponse> admins =
        department.getAdmins().stream()
            .map(serviceUtils::toAdminSummaryResponse)
            .filter(Objects::nonNull)
            .toList();
    return new DepartmentResponse(department.getId(), department.getName(), admins);
  }
}
