package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.api.AdminProfileResponse;
import com.oman.EmpPulse.user.api.EmployeeProfileResponse;
import com.oman.EmpPulse.user.api.MeResponse;
import com.oman.EmpPulse.user.api.UserCredential;
import com.oman.EmpPulse.user.api.UserDirectory;
import com.oman.EmpPulse.user.api.UserPreferencesResponse;
import com.oman.EmpPulse.user.api.UserResponse;
import com.oman.EmpPulse.user.dto.UserCreateRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService implements UserDirectory {

  private final UserRepository userRepository;
  private final AdminRepository adminRepository;
  private final EmployeeRepository employeeRepository;
  private final DepartmentApi departmentApi;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      AdminRepository adminRepository,
      EmployeeRepository employeeRepository,
      DepartmentApi departmentApi,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.adminRepository = adminRepository;
    this.employeeRepository = employeeRepository;
    this.departmentApi = departmentApi;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserCredential> findActiveByEmail(String email) {
    return userRepository
        .findByEmailAndIsDeletedFalse(email)
        .map(user -> new UserCredential(user.getId(), user.getPassHash(), authoritiesFor(user)));
  }

  private List<String> authoritiesFor(User user) {
    List<String> authorities = new ArrayList<>();
    if (user.isOwner()) {
      authorities.add("OWNER");
    }
    if (adminRepository.findByUserId(user.getId()).isPresent()) {
      authorities.add("ADMIN");
    }
    if (employeeRepository.findByUserId(user.getId()).isPresent()) {
      authorities.add("EMPLOYEE");
    }
    return authorities;
  }

  @Override
  @Transactional(readOnly = true)
  public MeResponse loadProfile(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    AdminProfileResponse adminProfile = null;
    Optional<Admin> adminOpt = adminRepository.findByUserId(userId);
    if (adminOpt.isPresent()) {
      Admin admin = adminOpt.get();
      List<Long> deptIds = admin.getDepartments().stream().map(Department::getId).toList();
      adminProfile = new AdminProfileResponse(admin.getId(), deptIds);
    }

    EmployeeProfileResponse employeeProfile = null;
    Optional<Employee> employeeOpt = employeeRepository.findByUserId(userId);
    if (employeeOpt.isPresent()) {
      Employee employee = employeeOpt.get();
      String deptName = null;
      if (employee.getDepartmentId() != null) {
        deptName = departmentApi.findNameById(employee.getDepartmentId()).orElse(null);
      }
      employeeProfile =
          new EmployeeProfileResponse(
              employee.getId(),
              employee.getDepartmentId(),
              deptName,
              employee.getVacationBalance());
    }

    UserResponse userResponse =
        new UserResponse(
            user.getId(),
            user.getName(),
            user.getSurname(),
            user.getEmail(),
            user.isOwner(),
            new UserPreferencesResponse(user.getTheme().name(), user.getLanguage().name()),
            employeeProfile,
            adminProfile);

    return new MeResponse(userResponse);
  }

  @Override
  @Transactional
  public void ensureOwnerExists(String email, String rawPassword) {
    if (userRepository.findByEmail(email).isPresent()) {
      return;
    }
    User user =
        new User(
            "System",
            "Owner",
            email,
            passwordEncoder.encode(rawPassword),
            UserTheme.LIGHT,
            UserLanguage.ENG);
    user.setOwner(true);
    userRepository.save(user);
  }

  @Transactional
  public void softDeleteUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    if (user.isOwner()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner cannot be deleted");
    }

    user.setDeleted(true);
    userRepository.save(user);
  }

  @Transactional
  public void createUser(UserCreateRequest req, boolean callerIsOwner) {
    boolean wantsAdmin = req.getAdminDepartmentIds() != null;

    if (!callerIsOwner && wantsAdmin) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Admins can only create employee accounts");
    }

    if (userRepository.findByEmail(req.getEmail()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
    }

    User user =
        new User(
            req.getName(),
            req.getSurname(),
            req.getEmail(),
            passwordEncoder.encode(req.getPassword()),
            UserTheme.LIGHT,
            UserLanguage.ENG);
    userRepository.save(user);

    if (req.getEmployeeDepartmentId() != null) {
      Employee employee =
          new Employee(
              user.getId(), req.getEmployeeDepartmentId(),
              req.getYearlyVacationBalance(), LocalDate.now());
      employeeRepository.save(employee);
    }

    if (wantsAdmin) {
      Admin admin = new Admin(user.getId());
      adminRepository.save(admin);
      departmentApi.assignAdminToDepartments(admin.getId(), req.getAdminDepartmentIds());
    }
  }
}
