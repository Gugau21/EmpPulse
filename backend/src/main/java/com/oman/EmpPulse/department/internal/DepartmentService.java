package com.oman.EmpPulse.department.internal;

import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.department.dto.DepartmentCreateRequest;
import com.oman.EmpPulse.department.dto.DepartmentListResponse;
import com.oman.EmpPulse.department.dto.DepartmentResponse;
import com.oman.EmpPulse.department.dto.DepartmentUpdateRequest;
import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.AdminSummaryResponse;
import com.oman.EmpPulse.user.api.EmployeeApi;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DepartmentService implements DepartmentApi {

  private final DepartmentRepository departmentRepository;
  private final AdminApi adminApi;
  private final EmployeeApi employeeApi;

  public DepartmentService(
      DepartmentRepository departmentRepository, AdminApi adminApi, EmployeeApi employeeApi) {
    this.departmentRepository = departmentRepository;
    this.adminApi = adminApi;
    this.employeeApi = employeeApi;
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
    return adminApi.overseesDepartment(userId, departmentId);
  }

  @Transactional
  public void deleteDepartment(Long departmentId) {
    Department department =
        departmentRepository
            .findById(departmentId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    if (employeeApi.hasEmployeesInDepartment(departmentId)) {
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
    List<Long> deptIds = adminApi.departmentIdsForAdminUser(userId);
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

  @Override
  @Transactional(readOnly = true)
  public Optional<String> findNameById(Long departmentId) {
    return departmentRepository.findById(departmentId).map(Department::getName);
  }

  @Override
  @Transactional
  public void assignAdminToDepartments(Long adminId, Collection<Long> departmentIds) {
    List<Department> departments = departmentRepository.findAllById(departmentIds);
    if (departments.size() != new HashSet<>(departmentIds).size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
    }
    Admin admin =
        adminApi
            .findById(adminId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));
    for (Department department : departments) {
      department.getAdmins().add(admin);
    }
  }

  private void validateNameAvailable(String newName, String currentName) {
    if (!newName.equals(currentName) && departmentRepository.existsByName(newName)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already in use");
    }
  }

  /**
   * Validates that admins being detached from this department will still oversee at least one other
   * department. Prevents leaving an admin with no departments.
   *
   * <p>This is a business rule: every admin must oversee at least one department. If an admin
   * currently oversees only this department, detaching them would violate this rule.
   *
   * @param currentAdmins the admins currently assigned to this department
   * @param newAdminIds the IDs of admins to assign (the new set)
   * @throws ResponseStatusException with 409 CONFLICT if any detached admin would have no remaining
   *     departments
   */
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
    List<Admin> admins = adminApi.findAllByIds(adminIds);
    if (admins.size() != new HashSet<>(adminIds).size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found");
    }
    return new HashSet<>(admins);
  }

  private DepartmentResponse toDepartmentResponse(Department department) {
    List<AdminSummaryResponse> admins =
        department.getAdmins().stream().map(adminApi::toSummary).filter(Objects::nonNull).toList();
    return new DepartmentResponse(department.getId(), department.getName(), admins);
  }
}
