package com.oman.EmpPulse.repository;

import com.oman.EmpPulse.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndIsDeletedFalse(String email);
}
