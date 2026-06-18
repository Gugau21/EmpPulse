package com.oman.EmpPulse.loggedhours.internal;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oman.EmpPulse.loggedhours.api.LoggedHours;

public interface LoggedHoursRepository extends JpaRepository<LoggedHours, Long> {

  List<LoggedHours> findAllByEmployeeIdAndDate(Long employeeId, LocalDate date);

  List<LoggedHours> findAllByEmployeeIdAndDateBetween(
      Long employeeId, LocalDate startDate, LocalDate endDate);

  List<LoggedHours> findAllByEmployeeIdOrderByDateDescStartTimeDesc(Long employeeId);

  List<LoggedHours> findAllByEmployeeIdInOrderByDateDescStartTimeDesc(Collection<Long> employeeIds);
}
