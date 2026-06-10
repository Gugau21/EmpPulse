package com.oman.EmpPulse.user.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BonusVacationDaysRepository extends JpaRepository<BonusVacationDays, Long> {
  List<BonusVacationDays> findByEmployeeId(Long employeeId);

  Optional<BonusVacationDays> findByEmployeeIdAndYear(Long employeeId, int year);
}
