package com.oman.EmpPulse.user.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndActiveTrue(String email);

  boolean existsByIsOwnerTrue();
}
