package com.oman.EmpPulse.repository;

import com.oman.EmpPulse.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long userId);
    boolean existsByDepartmentId(Long departmentId);
    List<Employee> findByDepartmentIdIn(Collection<Long> departmentIds);
}
