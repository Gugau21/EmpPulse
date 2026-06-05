package com.oman.EmpPulse.repository;

import com.oman.EmpPulse.entity.AdminDepartment;
import com.oman.EmpPulse.entity.AdminDepartmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminDepartmentRepository extends JpaRepository<AdminDepartment, AdminDepartmentId> {
    List<AdminDepartment> findByAdminId(Long adminId);
    List<AdminDepartment> findByDepartmentId(Long departmentId);
}
