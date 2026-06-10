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

  @Override
  @Transactional(readOnly = true)
  public Optional<String> findNameById(Long departmentId) {
    return departmentRepository.findById(departmentId).map(Department::getName);
  }

  @Override
  @Transactional
  public void setAdminDepartments(Long adminId, Collection<Long> departmentIds) {
    Admin admin =
        adminApi
            .findById(adminId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Data inconsistency"));

    Set<Long> targetIds = new HashSet<>(departmentIds);
    List<Department> targets = departmentRepository.findAllById(targetIds);
    if (targets.size() != targetIds.size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
    }

    // An empty departmentIds is intentional: it detaches the admin from every department, leaving
    // them with zero departments and deactivating them (admin.active = false)
    for (Department current : new HashSet<>(admin.getDepartments())) {
      if (!targetIds.contains(current.getId())) {
        current.getAdmins().remove(admin);
      }
    }

    for (Department target : targets) {
      target.getAdmins().add(admin);
    }
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
  public boolean isAdminOfDepartment(Long adminId, Long departmentId) {
    return adminApi.overseesDepartment(adminId, departmentId);
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
  public DepartmentListResponse getDepartmentsForAdmin(Long adminId) {
    List<Long> deptIds = adminApi.departmentIdsForAdminUser(adminId);
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
      department.setAdmins(loadAdminsFromIds(req.getAdminIds()));
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
      rejectDetachThatDeactivatesAdmin(department.getAdmins(), req.getAdminIds());
      department.setAdmins(loadAdminsFromIds(req.getAdminIds()));
    }
  }

  private void validateNameAvailable(String newName, String currentName) {
    if (!newName.equals(currentName) && departmentRepository.existsByName(newName)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already in use");
    }
  }

  /**
   * Prevents a department-roster edit from <em>accidentally</em> deactivating an admin. When {@link
   * #updateDepartment} removes an admin from this department, that admin must still oversee at
   * least one other department; otherwise they would drop to zero departments and be deactivated
   * ({@code admin.active} → false) as a side effect of editing a department.
   *
   * <p>This is intentionally <strong>not</strong> a global invariant. Deliberate deactivation is
   * supported via the user/role-edit path ({@link #setAdminDepartments} with an empty set), which
   * is permitted by design; this guard only stops the department-edit path from triggering it
   * unintentionally.
   *
   * @param currentAdmins the admins currently assigned to this department
   * @param newAdminIds the IDs of admins to assign (the new set)
   * @throws ResponseStatusException with 409 CONFLICT if a detached admin would be left with no
   *     remaining department
   */
  private void rejectDetachThatDeactivatesAdmin(Set<Admin> currentAdmins, List<Long> newAdminIds) {
    Set<Long> newAdminIdSet = new HashSet<>(newAdminIds);
    for (Admin admin : currentAdmins) {
      if (!newAdminIdSet.contains(admin.getId()) && admin.getDepartments().size() <= 1) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Cannot remove admin "
                + admin.getId()
                + " from their only department here; to deactivate this admin, edit their account"
                + " instead");
      }
    }
  }

  private Set<Admin> loadAdminsFromIds(List<Long> adminIds) {
    List<Admin> admins = adminApi.findAllByIds(adminIds);
    if (admins.size() != new HashSet<>(adminIds).size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found");
    }
    return new HashSet<>(admins);
  }

  private DepartmentResponse toDepartmentResponse(Department department) {
    List<AdminSummaryResponse> admins =
        department.getAdmins().stream().map(adminApi::toAdminSummaryResponse).toList();
    return new DepartmentResponse(department.getId(), department.getName(), admins);
  }
}
