package com.oman.EmpPulse.loggedhours.internal;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoggedHoursRepository extends JpaRepository<LoggedHours, Long> {

  List<LoggedHours> findAllByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
