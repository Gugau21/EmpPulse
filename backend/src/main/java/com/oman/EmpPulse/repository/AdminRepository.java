package com.oman.EmpPulse.repository;

import com.oman.EmpPulse.entity.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
  Optional<Admin> findByUserId(Long userId);
}
