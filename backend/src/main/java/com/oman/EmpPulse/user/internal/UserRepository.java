package com.oman.EmpPulse.user.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndActiveTrue(String email);

  Optional<User> findByIsOwnerTrue();

  List<User> findByIdIn(Collection<Long> ids);

  Optional<User> findById(Long id);

  boolean existsByIsOwnerTrue();
}
