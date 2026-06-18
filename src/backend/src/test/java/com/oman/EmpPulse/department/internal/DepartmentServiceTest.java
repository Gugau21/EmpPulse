package com.oman.EmpPulse.department.internal;

import static com.oman.EmpPulse.support.Fixtures.admin;
import static com.oman.EmpPulse.support.Fixtures.department;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.department.dto.DepartmentCreateRequest;
import com.oman.EmpPulse.department.dto.DepartmentUpdateRequest;
import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.EmployeeApi;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

  @Mock private DepartmentRepository departmentRepository;
  @Mock private AdminApi adminApi;
  @Mock private EmployeeApi employeeApi;

  @InjectMocks private DepartmentService departmentService;

  @Test
  void createDepartmentRejectsDuplicateName() {
    DepartmentCreateRequest req = new DepartmentCreateRequest();
    req.setName("Engineering");
    when(departmentRepository.existsByName("Engineering")).thenReturn(true);

    assertStatus(
        HttpStatus.CONFLICT, () -> departmentService.createDepartment(req), "already in use");
  }

  @Test
  void createDepartmentAlwaysAddsOwnerAsAdmin() {
    DepartmentCreateRequest req = new DepartmentCreateRequest();
    req.setName("Engineering");
    req.setAdminIds(List.of(2L));
    when(departmentRepository.existsByName("Engineering")).thenReturn(false);
    when(adminApi.findAllByIds(List.of(2L))).thenReturn(List.of(admin(2L)));
    when(adminApi.getOwnerAdmin()).thenReturn(Optional.of(admin(1L)));

    departmentService.createDepartment(req);

    verify(departmentRepository)
        .save(org.mockito.ArgumentMatchers.argThat(saved -> hasAdmin(saved, 1L)));
  }

  @Test
  void updateDepartmentRejectsDetachThatWouldDeactivateAdmin() {
    Department currentDepartment = department(10L, "Engineering", false);
    Admin detachableAdmin = admin(2L);
    detachableAdmin.getDepartments().add(currentDepartment);
    currentDepartment.setAdmins(Set.of(detachableAdmin));

    DepartmentUpdateRequest req = new DepartmentUpdateRequest();
    req.setAdminIds(List.of());
    when(departmentRepository.findById(10L)).thenReturn(Optional.of(currentDepartment));
    when(adminApi.getOwnerAdmin()).thenReturn(Optional.of(admin(1L)));

    assertStatus(
        HttpStatus.CONFLICT,
        () -> departmentService.updateDepartment(10L, req),
        "Cannot remove admin");
  }

  @Test
  void deleteDepartmentRejectsDefaultAndDepartmentWithEmployees() {
    Department defaultDept = department(10L, "Default Department", true);
    when(departmentRepository.findById(10L)).thenReturn(Optional.of(defaultDept));

    assertStatus(
        HttpStatus.CONFLICT,
        () -> departmentService.deleteDepartment(10L),
        "Cannot delete the default department");

    Department regularDept = department(11L, "Engineering", false);
    when(departmentRepository.findById(11L)).thenReturn(Optional.of(regularDept));
    when(employeeApi.hasEmployeesInDepartment(11L)).thenReturn(true);

    assertStatus(
        HttpStatus.CONFLICT,
        () -> departmentService.deleteDepartment(11L),
        "employees are assigned");
  }

  @Test
  void ensureDefaultDepartmentExistsIsIdempotent() {
    when(departmentRepository.existsByIsDefaultTrue()).thenReturn(true);

    departmentService.ensureDefaultDepartmentExists();

    verify(departmentRepository, never()).save(org.mockito.ArgumentMatchers.any(Department.class));
  }

  private boolean hasAdmin(Department department, Long id) {
    return department.getAdmins().stream().anyMatch(admin -> admin.getId().equals(id));
  }

  private void assertStatus(HttpStatus status, Runnable action, String messageContains) {
    assertThatThrownBy(action::run)
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            throwable -> {
              ResponseStatusException ex = (ResponseStatusException) throwable;
              assertThat(ex.getStatusCode()).isEqualTo(status);
              assertThat(ex.getReason()).contains(messageContains);
            });
  }
}
