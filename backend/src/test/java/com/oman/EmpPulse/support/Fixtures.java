package com.oman.EmpPulse.support;

import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.leave.internal.Leave;
import com.oman.EmpPulse.leave.internal.LeaveStatus;
import com.oman.EmpPulse.leave.internal.LeaveType;
import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.internal.Employee;
import com.oman.EmpPulse.user.internal.User;
import com.oman.EmpPulse.user.internal.UserLanguage;
import com.oman.EmpPulse.user.internal.UserTheme;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import org.springframework.test.util.ReflectionTestUtils;

public final class Fixtures {

  private Fixtures() {}

  public static Leave leave(
      Long id,
      Long employeeId,
      LeaveType type,
      LeaveStatus status,
      boolean paid,
      LocalDate startDate,
      LocalDate endDate,
      String reason) {
    Leave leave = new Leave(employeeId, type, startDate, endDate, paid, status, reason, null, null);
    ReflectionTestUtils.setField(leave, "id", id);
    ReflectionTestUtils.setField(leave, "createdAt", OffsetDateTime.now().minusDays(1));
    ReflectionTestUtils.setField(leave, "updatedAt", OffsetDateTime.now());
    return leave;
  }

  public static Employee employee(Long id, Long departmentId, int vacationBalance, boolean active) {
    Employee employee = new Employee(id, departmentId, null, vacationBalance);
    ReflectionTestUtils.setField(employee, "active", active);
    return employee;
  }

  public static Admin admin(Long id, Department... departments) {
    Admin admin = new Admin(id);
    admin.getDepartments().addAll(Set.of(departments));
    ReflectionTestUtils.setField(admin, "active", true);
    return admin;
  }

  public static User user(
      Long id,
      String name,
      String surname,
      String email,
      String passwordHash,
      boolean owner,
      boolean active) {
    User user = new User(name, surname, email, passwordHash, UserTheme.light, UserLanguage.en);
    user.setId(id);
    user.setOwner(owner);
    ReflectionTestUtils.setField(user, "active", active);
    return user;
  }

  public static Department department(Long id, String name, boolean isDefault) {
    Department department = new Department(name);
    ReflectionTestUtils.setField(department, "id", id);
    department.setDefault(isDefault);
    return department;
  }
}
