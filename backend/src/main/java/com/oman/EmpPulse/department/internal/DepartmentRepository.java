package com.oman.EmpPulse.department.internal;

import com.oman.EmpPulse.department.api.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
  boolean existsByName(String name);

  boolean existsByIsDefaultTrue();
}
