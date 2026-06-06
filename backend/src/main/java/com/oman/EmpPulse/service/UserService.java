package com.oman.EmpPulse.service;

import com.oman.EmpPulse.dto.UserCreateRequest;
import com.oman.EmpPulse.dto.response.*;
import com.oman.EmpPulse.entity.*;
import com.oman.EmpPulse.repository.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final AdminRepository adminRepository;
  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      AdminRepository adminRepository,
      EmployeeRepository employeeRepository,
      DepartmentRepository departmentRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.adminRepository = adminRepository;
    this.employeeRepository = employeeRepository;
    this.departmentRepository = departmentRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional(readOnly = true)
  public MeResponse buildMeResponse(Long userId) {
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
        deptName =
            departmentRepository
                .findById(employee.getDepartmentId())
                .map(Department::getName)
                .orElse(null);
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

      List<Long> deptIds = req.getAdminDepartmentIds();
      List<Department> departments = departmentRepository.findAllById(deptIds);
      if (departments.size() != new HashSet<>(deptIds).size()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
      }
      // Department owns the @ManyToMany relationship
      for (Department department : departments) {
        department.getAdmins().add(admin);
      }
    }
  }
}
