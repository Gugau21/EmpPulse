package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.user.api.Admin;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
  List<Admin> findByActiveTrue();

  List<Admin> findAllByDepartmentsIdIn(Collection<Long> departmentIds);
}
