package com.oman.EmpPulse.user.internal;

import static com.oman.EmpPulse.support.Fixtures.admin;
import static com.oman.EmpPulse.support.Fixtures.department;
import static com.oman.EmpPulse.support.Fixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.dto.AdminListResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @Mock private AdminRepository adminRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private AdminService adminService;

  @Test
  void overseesDepartmentReturnsFalseWhenAdminMissing() {
    when(adminRepository.findById(1L)).thenReturn(Optional.empty());

    assertThat(adminService.overseesDepartment(1L, 10L)).isFalse();
  }

  @Test
  void departmentIdsForAdminUserRejectsMissingAdmin() {
    when(adminRepository.findById(1L)).thenReturn(Optional.empty());

    assertStatus(
        HttpStatus.FORBIDDEN, () -> adminService.departmentIdsForAdminUser(1L), "Access denied");
  }

  @Test
  void getAllAdminsExcludesOwnerFromResponse() {
    Admin ownerAdmin = admin(1L, department(10L, "A", false));
    Admin normalAdmin = admin(2L, department(20L, "B", false));
    when(userRepository.findByIsOwnerTrue())
        .thenReturn(Optional.of(user(1L, "Owner", "X", "o@x.com", "h", true, true)));
    when(adminRepository.findByActiveTrue()).thenReturn(List.of(ownerAdmin, normalAdmin));
    when(userRepository.findById(2L))
        .thenReturn(Optional.of(user(2L, "Admin", "Y", "a@x.com", "h", false, true)));

    AdminListResponse response = adminService.getAllAdmins();

    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().getFirst().getId()).isEqualTo(2L);
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
