package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.user.api.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {}
