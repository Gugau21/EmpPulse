package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.defaulthours.api.DefaultHoursApi;
import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.leave.api.ActiveLeaveResponse;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.api.AdminProfileResponse;
import com.oman.EmpPulse.user.api.EmployeeProfileResponse;
import com.oman.EmpPulse.user.api.UserApi;
import com.oman.EmpPulse.user.api.UserCredential;
import com.oman.EmpPulse.user.api.UserPreferencesResponse;
import com.oman.EmpPulse.user.api.UserResponse;
import com.oman.EmpPulse.user.dto.BonusVacationDayRequest;
import com.oman.EmpPulse.user.dto.BonusVacationDaysResponse;
import com.oman.EmpPulse.user.dto.PasswordChangeRequest;
import com.oman.EmpPulse.user.dto.PreferencesUpdateRequest;
import com.oman.EmpPulse.user.dto.UserCreateRequest;
import com.oman.EmpPulse.user.dto.UserUpdateRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService implements UserApi {

  private final UserRepository userRepository;
  private final AdminRepository adminRepository;
  private final EmployeeRepository employeeRepository;
  private final BonusVacationDaysRepository bonusVacationDaysRepository;
  private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
  private final DepartmentApi departmentApi;
  private final DefaultHoursApi defaultHoursApi;
  private final PasswordEncoder passwordEncoder;
  private final LeaveApi leaveApi;

  public UserService(
      UserRepository userRepository,
      AdminRepository adminRepository,
      EmployeeRepository employeeRepository,
      BonusVacationDaysRepository bonusVacationDaysRepository,
      FindByIndexNameSessionRepository<? extends Session> sessionRepository,
      DepartmentApi departmentApi,
      @Lazy DefaultHoursApi defaultHoursApi,
      PasswordEncoder passwordEncoder,
      @Lazy LeaveApi leaveApi) {
    this.userRepository = userRepository;
    this.adminRepository = adminRepository;
    this.employeeRepository = employeeRepository;
    this.bonusVacationDaysRepository = bonusVacationDaysRepository;
    this.sessionRepository = sessionRepository;
    this.departmentApi = departmentApi;
    this.defaultHoursApi = defaultHoursApi;
    this.passwordEncoder = passwordEncoder;
    this.leaveApi = leaveApi;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserCredential> findActiveByEmail(String email) {
    return userRepository
        .findByEmailAndActiveTrue(email)
        .map(user -> new UserCredential(user.getId(), user.getPassHash(), authoritiesFor(user)));
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse loadProfile(Long userId) {
    return buildUserResponse(userId);
  }

  @Override
  @Transactional
  public void ensureOwnerExists(String email, String rawPassword) {
    if (userRepository.existsByIsOwnerTrue()) {
      return;
    }
    User owner =
        new User(
            "System",
            "Owner",
            email,
            passwordEncoder.encode(rawPassword),
            UserTheme.light,
            UserLanguage.en);
    owner.setOwner(true);
    userRepository.save(owner);

    adminRepository.save(new Admin(owner.getId()));
    departmentApi.setAdminDepartments(owner.getId(), departmentApi.findAllDepartmentIds());
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findByIdIn(Collection<Long> userIds) {
    return userRepository.findByIdIn(userIds);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(Long userId) {
    return userRepository.findById(userId);
  }

  private User getUserById(Long userId) {
    return userRepository
        .findById(userId)
        .filter(User::isActive)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private List<String> authoritiesFor(User user) {
    List<String> authorities = new ArrayList<>();
    if (user.isOwner()) {
      authorities.add("OWNER");
    }
    boolean activeAdmin =
        adminRepository.findById(user.getId()).filter(Admin::isActive).isPresent();
    if (user.isOwner() || activeAdmin) { // .isOwner() in case there are no departments
      authorities.add("ADMIN");
    }
    if (employeeRepository.findById(user.getId()).filter(Employee::isActive).isPresent()) {
      authorities.add("EMPLOYEE");
    }
    return authorities;
  }

  @Transactional(readOnly = true)
  public UserResponse getUserProfile(Long userId, Long callerId, boolean callerIsOwner) {
    if (!callerIsOwner) {
      Employee employee = requireActiveEmployee(userId);
      verifyAdminOverseesDepartment(callerId, employee.getDepartmentId());
    }

    return buildUserResponse(userId);
  }

  private UserResponse buildUserResponse(Long userId) {
    User user = getUserById(userId);

    AdminProfileResponse adminProfile = null;
    Optional<Admin> adminOpt = adminRepository.findById(userId);
    if (adminOpt.isPresent() && !user.isOwner()) {
      Admin admin = adminOpt.get();
      if (admin.isActive()) {
        List<Long> deptIds = admin.getDepartments().stream().map(Department::getId).toList();
        adminProfile = new AdminProfileResponse(admin.getId(), deptIds);
      }
    }

    EmployeeProfileResponse employeeProfile = null;
    Optional<Employee> employeeOpt = employeeRepository.findById(userId);
    if (employeeOpt.isPresent()) {
      Employee employee = employeeOpt.get();
      if (employee.isActive()) {
        String deptName = departmentApi.findNameById(employee.getDepartmentId()).orElse(null);
        Map<Long, ActiveLeaveResponse> activeLeaves =
            leaveApi.findActiveLeavesByEmployeeIds(List.of(employee.getId()));

        int year = LocalDate.now().getYear();
        int bonus =
            bonusVacationDaysRepository
                .findByEmployeeIdAndYear(employee.getId(), year)
                .map(BonusVacationDays::getDays)
                .orElse(0);
        int used = leaveApi.countUsedVacationDays(employee.getId(), year);
        int vacationBalance = employee.getVacationBalance() + bonus - used;

        employeeProfile =
            new EmployeeProfileResponse(
                employee.getId(),
                employee.getDepartmentId(),
                deptName,
                employee.getVacationBalance(),
                vacationBalance,
                activeLeaves.get(employee.getId()));
      }
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

  @Transactional
  public void softDeleteUser(Long userId) {
    User user = getUserById(userId);
    if (user.isOwner()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Owner cannot be deleted");
    }

    Optional<Employee> employeeOpt = employeeRepository.findById(userId);
    if (employeeOpt.isPresent()) {
      Employee employee = employeeOpt.get();
      employee.setDepartmentId(null);
    }

    Optional<Admin> adminOpt = adminRepository.findById(userId);
    if (adminOpt.isPresent()) {
      departmentApi.setAdminDepartments(userId, List.of());
    }

    sessionRepository
        .findByPrincipalName(userId.toString())
        .keySet()
        .forEach(sessionRepository::deleteById);
  }

  @Transactional
  public UserPreferencesResponse updatePreferences(Long userId, PreferencesUpdateRequest req) {
    User user = getUserById(userId);

    if (req.getTheme() != null) {
      user.setTheme(parseTheme(req.getTheme()));
    }
    if (req.getLanguage() != null) {
      user.setLanguage(parseLanguage(req.getLanguage()));
    }

    userRepository.save(user);
    return new UserPreferencesResponse(user.getTheme().name(), user.getLanguage().name());
  }

  private UserTheme parseTheme(String theme) {
    try {
      return UserTheme.valueOf(theme.toLowerCase());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid theme");
    }
  }

  private UserLanguage parseLanguage(String language) {
    try {
      return UserLanguage.valueOf(language.toLowerCase());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid language");
    }
  }

  @Transactional
  public void changeMyPassword(Long userId, PasswordChangeRequest req, String currentSessionId) {
    if (!StringUtils.hasText(req.getCurrentPassword())
        || !StringUtils.hasText(req.getNewPassword())
        || !StringUtils.hasText(req.getConfirmNewPassword())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Current password, new password and confirmation are required");
    }

    User user = getUserById(userId);

    if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassHash())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    }

    if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
    }

    if (passwordEncoder.matches(req.getNewPassword(), user.getPassHash())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "New password must differ from the current password");
    }

    user.setPassHash(passwordEncoder.encode(req.getNewPassword()));

    // Invalidate all other sessions for this user, keeping the caller's current session.
    sessionRepository.findByPrincipalName(userId.toString()).keySet().stream()
        .filter(sessionId -> !sessionId.equals(currentSessionId))
        .forEach(sessionRepository::deleteById);

    // TODO(feat/notification): once the notification module is merged, notify the user, e.g.
    // notificationApi.sendPasswordChangedNotification(
    //     new NotificationRecipient(user.getEmail(), user.getName()));
  }

  @Override
  @Transactional
  public void resetPassword(Long userId, String rawNewPassword) {
    User user = getUserById(userId);
    user.setPassHash(passwordEncoder.encode(rawNewPassword));

    // Reset is unauthenticated, so invalidate every session; the user must log in again.
    sessionRepository
        .findByPrincipalName(userId.toString())
        .keySet()
        .forEach(sessionRepository::deleteById);
  }

  @Transactional
  public Long createUser(UserCreateRequest req, Long callerUserId, boolean callerIsOwner) {
    boolean reqToCreateEmployee = (req.getEmployeeDepartmentId() != null);
    boolean reqToCreateAdmin =
        (req.getAdminDepartmentIds() != null) && !req.getAdminDepartmentIds().isEmpty();

    requireUserFields(req);

    if (!reqToCreateAdmin && !reqToCreateEmployee) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot create an account with no role");
    }

    if (!callerIsOwner && reqToCreateAdmin) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Admins can only create employee accounts");
    }

    if (reqToCreateEmployee) {
      requireDepartmentExists(req.getEmployeeDepartmentId());
      verifyAdminOverseesDepartment(callerUserId, req.getEmployeeDepartmentId());
    }

    if (userRepository.findByEmailAndActiveTrue(req.getEmail()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
    }

    User user =
        new User(
            req.getName(),
            req.getSurname(),
            req.getEmail(),
            passwordEncoder.encode(req.getPassword()),
            UserTheme.light,
            UserLanguage.en);
    userRepository.save(user);

    if (reqToCreateEmployee) {
      requireVacationBalanceForCreation(req.getYearlyVacationBalance());
      Employee employee =
          new Employee(
              user.getId(),
              req.getEmployeeDepartmentId(),
              defaultHoursApi.inheritDepartmentSchedule(req.getEmployeeDepartmentId()),
              req.getYearlyVacationBalance());
      employeeRepository.save(employee);
    }

    if (reqToCreateAdmin) {
      Admin admin = new Admin(user.getId());
      adminRepository.save(admin);
      departmentApi.setAdminDepartments(admin.getId(), req.getAdminDepartmentIds());
    }

    return user.getId();
  }

  private void requireUserFields(UserCreateRequest req) {
    if (!StringUtils.hasText(req.getName())
        || !StringUtils.hasText(req.getSurname())
        || !StringUtils.hasText(req.getEmail())
        || !StringUtils.hasText(req.getPassword())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Name, surname, email and password are required");
    }
  }

  @Transactional(readOnly = true)
  public BonusVacationDaysResponse getBonusVacationDaysForEmployee(Long userId, Long callerId) {
    Employee employee = requireActiveEmployee(userId);
    verifyAdminOverseesDepartment(callerId, employee.getDepartmentId());

    int year = LocalDate.now().getYear();
    int days =
        bonusVacationDaysRepository
            .findByEmployeeIdAndYear(employee.getId(), year)
            .map(BonusVacationDays::getDays)
            .orElse(0);

    return new BonusVacationDaysResponse(year, days);
  }

  @Transactional
  public void updateBonusVacationDays(Long userId, BonusVacationDayRequest req, Long callerUserId) {
    Employee employee = requireActiveEmployee(userId);
    verifyAdminOverseesDepartment(callerUserId, employee.getDepartmentId());

    requireBonusVacationDayFields(req);
    requireNonNegativeBonusVacationDays(req.getDays());

    int year = req.getYear();
    int days = req.getDays();
    Optional<BonusVacationDays> existing =
        bonusVacationDaysRepository.findByEmployeeIdAndYear(employee.getId(), year);

    if (days == 0) {
      existing.ifPresent(bonusVacationDaysRepository::delete);
      return;
    }

    if (existing.isPresent()) {
      BonusVacationDays bonusVacationDays = existing.get();
      bonusVacationDays.setDays(days);
      bonusVacationDaysRepository.save(bonusVacationDays);
    } else {
      bonusVacationDaysRepository.save(new BonusVacationDays(employee.getId(), year, days));
    }
  }

  private Employee requireActiveEmployee(Long userId) {
    Employee employee =
        employeeRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
    if (!employee.isActive()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
    return employee;
  }

  private void requireBonusVacationDayFields(BonusVacationDayRequest req) {
    if (req.getYear() == null || req.getDays() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year and days are required");
    }
  }

  private void requireNonNegativeBonusVacationDays(int days) {
    if (days < 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Bonus vacation days must be greater or equal to 0");
    }
  }

  @Transactional
  public void updateUser(
      Long userId, UserUpdateRequest req, Long callerUserId, boolean callerIsOwner) {

    verifyAdminNotEditingUserData(req, callerIsOwner);
    User user = getUserById(userId);
    if (callerIsOwner) {
      updateUserSpecificData(user, req);
    }

    boolean hasAdminUpdate = req.getAdminDepartmentIds() != null;
    boolean hasEmployeeUpdate =
        req.hasChangeEmployeeDepartment() || (req.getYearlyVacationBalance() != null);

    if (hasEmployeeUpdate) {
      handleEmployeeUpdate(userId, req, callerUserId, callerIsOwner);
    }

    if (hasAdminUpdate) {
      Optional<Admin> adminOpt = adminRepository.findById(userId);
      if (adminOpt.isPresent()) {
        updateAdminSpecificData(adminOpt.get(), req);
      } else if (!req.getAdminDepartmentIds().isEmpty()) {
        attachAdminToUser(userId, req);
      }
    }
  }

  private void verifyAdminNotEditingUserData(UserUpdateRequest req, boolean callerIsOwner) {
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
  }

  private void updateUserSpecificData(User user, UserUpdateRequest req) {
    if (req.getName() != null) {
      user.setName(req.getName());
    }
    if (req.getSurname() != null) {
      user.setSurname(req.getSurname());
    }
    if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
      if (userRepository.findByEmailAndActiveTrue(req.getEmail()).isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
      }
      user.setEmail(req.getEmail());
    }
    if (req.getPassword() != null) {
      user.setPassHash(passwordEncoder.encode(req.getPassword()));
    }
    userRepository.save(user);
  }

  private void handleEmployeeUpdate(
      Long userId, UserUpdateRequest req, Long callerUserId, boolean callerIsOwner) {
    Optional<Employee> employeeOpt = employeeRepository.findById(userId);
    if (employeeOpt.isEmpty()) {
      if (callerIsOwner && canUpdateUserToAttachEmployee(req)) {
        attachEmployeeToUser(userId, req);
      }
      return;
    }

    Employee employee = employeeOpt.get();
    boolean attemptingToAssignRealDepartment =
        req.hasChangeEmployeeDepartment() && (req.getEmployeeDepartmentId() != null);

    if (attemptingToAssignRealDepartment) {
      requireDepartmentExists(req.getEmployeeDepartmentId());
    }
    if (!callerIsOwner) {
      verifyAdminOverseesDepartment(callerUserId, employee.getDepartmentId());
      if (attemptingToAssignRealDepartment) {
        verifyAdminOverseesDepartment(callerUserId, req.getEmployeeDepartmentId());
      }
    }
    updateEmployeeSpecificData(employee, req, callerUserId, callerIsOwner);
  }

  private void updateEmployeeSpecificData(
      Employee employee, UserUpdateRequest req, Long callerUserId, boolean callerIsOwner) {
    if (req.hasChangeEmployeeDepartment()) {
      Long newDepartmentId = req.getEmployeeDepartmentId();
      authorizeDepartmentDetach(newDepartmentId, callerIsOwner);
      employee.setDepartmentId(newDepartmentId);
    }
    if (req.getYearlyVacationBalance() != null) {
      requireNonNegativeVacationBalance(req.getYearlyVacationBalance());
      employee.setVacationBalance(req.getYearlyVacationBalance());
    }
    employeeRepository.save(employee);
  }

  private void authorizeDepartmentDetach(Long newDepartmentId, boolean callerIsOwner) {
    if (callerIsOwner) {
      return;
    }
    if (newDepartmentId == null) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only the owner can detach an employee from a department");
    }
  }

  private boolean canUpdateUserToAttachEmployee(UserUpdateRequest req) {
    if (req.getEmployeeDepartmentId() == null) {
      return false;
    }
    requireVacationBalanceForCreation(req.getYearlyVacationBalance());
    return true;
  }

  private void attachEmployeeToUser(Long userId, UserUpdateRequest req) {
    requireDepartmentExists(req.getEmployeeDepartmentId());
    Employee employee =
        new Employee(
            userId,
            req.getEmployeeDepartmentId(),
            defaultHoursApi.inheritDepartmentSchedule(req.getEmployeeDepartmentId()),
            req.getYearlyVacationBalance());
    employeeRepository.save(employee);
  }

  private void updateAdminSpecificData(Admin admin, UserUpdateRequest req) {
    departmentApi.setAdminDepartments(admin.getId(), req.getAdminDepartmentIds());
  }

  private void attachAdminToUser(Long userId, UserUpdateRequest req) {
    Admin admin = new Admin(userId);
    adminRepository.save(admin);
    departmentApi.setAdminDepartments(admin.getId(), req.getAdminDepartmentIds());
  }

  private void requireDepartmentExists(Long departmentId) {
    if (departmentApi.findNameById(departmentId).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
    }
  }

  private void verifyAdminOverseesDepartment(Long callerUserId, Long departmentId) {
    Admin callerAdmin =
        adminRepository
            .findById(callerUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not an admin"));
    boolean oversees =
        callerAdmin.getDepartments().stream().anyMatch(d -> d.getId().equals(departmentId));
    if (!oversees) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Admin does not oversee target department");
    }
  }

  private void requireVacationBalanceForCreation(Integer vacationBalance) {
    if (vacationBalance == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Yearly vacation balance is required when creating an employee. "
              + "You can set 0 if you do not wish to track Vacation Balance.");
    }
    requireNonNegativeVacationBalance(vacationBalance);
  }

  private void requireNonNegativeVacationBalance(int vacationBalance) {
    if (vacationBalance < 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Yearly vacation balance must be greater or equal to 0");
    }
  }
}
