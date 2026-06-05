package com.oman.EmpPulse.repository;

import com.oman.EmpPulse.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUserId(Long userId);
}
