package com.oman.EmpPulse.loggedhours.internal;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoggedHoursRepository extends JpaRepository<LoggedHours, Long> {

  List<LoggedHours> findAllByEmployeeIdAndDate(Long employeeId, LocalDate date);

  List<LoggedHours> findAllByEmployeeIdAndDateBetween(
      Long employeeId, LocalDate startDate, LocalDate endDate);

  List<LoggedHours> findAllByEmployeeIdOrderByDateDescStartTimeDesc(Long employeeId);
}
