package com.oman.EmpPulse.user.internal;

import com.oman.EmpPulse.defaulthours.internal.WeekSchedule;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  boolean existsByDepartmentId(Long departmentId);

  List<Employee> findByActiveTrue();

  List<Employee> findByDepartmentIdIn(Collection<Long> departmentIds);

  @Query(
      "select distinct ws from WeekSchedule ws "
          + "where ws.id in (select e.weekScheduleId from Employee e where e.id in :employeeIds)")
  List<WeekSchedule> findWeekScheduleByEmployeeIds(
      @Param("employeeIds") Collection<Long> employeeIds);
}
