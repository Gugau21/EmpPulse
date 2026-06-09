package com.oman.EmpPulse.user.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  boolean existsByDepartmentId(Long departmentId);

  List<Employee> findByDepartmentIdIn(Collection<Long> departmentIds);
}
