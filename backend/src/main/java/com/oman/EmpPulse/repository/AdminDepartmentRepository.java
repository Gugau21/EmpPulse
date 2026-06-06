package com.oman.EmpPulse.repository;

import com.oman.EmpPulse.entity.AdminDepartment;
import com.oman.EmpPulse.entity.AdminDepartmentId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminDepartmentRepository
    extends JpaRepository<AdminDepartment, AdminDepartmentId> {
  List<AdminDepartment> findByAdminId(Long adminId);

  List<AdminDepartment> findByDepartmentId(Long departmentId);
}
