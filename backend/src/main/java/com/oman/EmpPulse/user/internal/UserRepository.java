package com.oman.EmpPulse.user.internal;

import java.util.Optional;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndActiveTrue(String email);

  List<User> findByIdIn(Collection<Long> ids);

  Optional<User> findById(Long id);

  boolean existsByIsOwnerTrue();
}
