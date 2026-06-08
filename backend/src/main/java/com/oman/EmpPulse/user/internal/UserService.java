package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.api.AdminProfileResponse;
import com.oman.EmpPulse.user.api.EmployeeProfileResponse;
import com.oman.EmpPulse.user.api.UserCredential;
import com.oman.EmpPulse.user.api.UserDirectory;
import com.oman.EmpPulse.user.api.UserPreferencesResponse;
import com.oman.EmpPulse.user.api.UserResponse;
import com.oman.EmpPulse.user.dto.UserCreateRequest;
import com.oman.EmpPulse.user.dto.UserUpdateRequest;
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
  public UserResponse loadProfile(Long userId) {
    return buildUserResponse(userId);
  }

  @Transactional(readOnly = true)
  public UserResponse getUserProfile(Long userId, Long callerUserId, boolean callerIsOwner) {
    if (!callerIsOwner) {
      Optional<Employee> employeeOpt = employeeRepository.findByUserId(userId);
      Employee employee =
          employeeOpt.orElseThrow(
              () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
      if (employee.getDepartmentId() == null) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
      verifyAdminOverseesDepartment(callerUserId, employee.getDepartmentId());
    }

    userRepository
        .findById(userId)
        .filter(u -> !u.isDeleted())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    return buildUserResponse(userId);
  }

  private UserResponse buildUserResponse(Long userId) {
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

    return new UserResponse(
        user.getId(),
        user.getName(),
        user.getSurname(),
        user.getEmail(),
        user.isOwner(),
        new UserPreferencesResponse(user.getTheme().name(), user.getLanguage().name()),
        employeeProfile,
        adminProfile);
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
      requireVacationBalanceForCreation(req.getYearlyVacationBalance());
      Employee employee =
          new Employee(
              user.getId(), req.getEmployeeDepartmentId(),
              req.getYearlyVacationBalance(), LocalDate.now());
      employeeRepository.save(employee);
    }

    if (wantsAdmin) {
      Admin admin = new Admin(user.getId());
      adminRepository.save(admin);
      departmentApi.setAdminDepartments(admin.getId(), req.getAdminDepartmentIds());
    }
  }

  @Transactional
  public void updateUser(
      Long userId, UserUpdateRequest req, Long callerUserId, boolean callerIsOwner) {
    if (!callerIsOwner) {
      boolean hasOwnerOnlyFields =
          req.getName() != null
              || req.getSurname() != null
              || req.getEmail() != null
              || req.getPassword() != null
              || req.getAdminDepartmentIds() != null;
      if (hasOwnerOnlyFields) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Admins can only update employee department and vacation balance");
      }
    }

    User user =
        userRepository
            .findById(userId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    updateUserSpecificInfo(user, req);

    boolean hasEmployeeUpdate =
        req.isChangeEmployeeDepartment() || req.getYearlyVacationBalance() != null;
    if (hasEmployeeUpdate) {
      Optional<Employee> employeeOpt = employeeRepository.findByUserId(userId);
      if (employeeOpt.isPresent()) {
        updateEmployeeSpecificInfo(employeeOpt.get(), req, callerUserId, callerIsOwner);
      } else if (callerIsOwner && canCreateEmployee(req)) {
        attachEmployeeToUser(userId, req);
      }
    }

    if (req.getAdminDepartmentIds() != null) {
      Optional<Admin> adminOpt = adminRepository.findByUserId(userId);
      if (adminOpt.isPresent()) {
        updateAdminSpecificInfo(adminOpt.get(), req);
      } else if (callerIsOwner && !req.getAdminDepartmentIds().isEmpty()) {
        attachAdminToUser(userId, req);
      }
    }
  }

  private void verifyAdminOverseesDepartment(Long callerUserId, Long departmentId) {
    Admin callerAdmin =
        adminRepository
            .findByUserId(callerUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not an admin"));
    boolean oversees =
        callerAdmin.getDepartments().stream().anyMatch(d -> d.getId().equals(departmentId));
    if (!oversees) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Admin does not oversee target department");
    }
  }

  private void updateUserSpecificInfo(User user, UserUpdateRequest req) {
    if (req.getName() != null) {
      user.setName(req.getName());
    }
    if (req.getSurname() != null) {
      user.setSurname(req.getSurname());
    }
    if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
      if (userRepository.findByEmailAndIsDeletedFalse(req.getEmail()).isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
      }
      user.setEmail(req.getEmail());
    }
    if (req.getPassword() != null) {
      user.setPassHash(passwordEncoder.encode(req.getPassword()));
    }
    userRepository.save(user);
  }

  private void updateEmployeeSpecificInfo(
      Employee employee, UserUpdateRequest req, Long callerUserId, boolean callerIsOwner) {
    if (req.isChangeEmployeeDepartment()) {
      Long newDepartmentId = req.getEmployeeDepartmentId(); // null = detach
      authorizeDepartmentChange(newDepartmentId, callerUserId, callerIsOwner);
      employee.setDepartmentId(newDepartmentId);
    }
    if (req.getYearlyVacationBalance() != null) {
      employee.setVacationBalance(req.getYearlyVacationBalance());
    }
    employeeRepository.save(employee);
  }

  private void authorizeDepartmentChange(
      Long newDepartmentId, Long callerUserId, boolean callerIsOwner) {
    if (callerIsOwner) {
      return;
    }
    if (newDepartmentId == null) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only the owner can detach an employee from a department");
    }
    verifyAdminOverseesDepartment(callerUserId, newDepartmentId);
  }

  private boolean canCreateEmployee(UserUpdateRequest req) {
    if (!req.isChangeEmployeeDepartment() || req.getEmployeeDepartmentId() == null) {
      return false;
    }
    requireVacationBalanceForCreation(req.getYearlyVacationBalance());
    return true;
  }

  private void attachEmployeeToUser(Long userId, UserUpdateRequest req) {
    Employee employee =
        new Employee(
            userId, req.getEmployeeDepartmentId(), req.getYearlyVacationBalance(), LocalDate.now());
    employeeRepository.save(employee);
  }

  private void requireVacationBalanceForCreation(Integer vacationBalance) {
    if (vacationBalance == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Yearly vacation balance is required when creating an employee");
    }
  }

  private void updateAdminSpecificInfo(Admin admin, UserUpdateRequest req) {
    departmentApi.setAdminDepartments(admin.getId(), req.getAdminDepartmentIds());
  }

  private void attachAdminToUser(Long userId, UserUpdateRequest req) {
    Admin admin = new Admin(userId);
    adminRepository.save(admin);
    departmentApi.setAdminDepartments(admin.getId(), req.getAdminDepartmentIds());
  }
}
