package com.oman.EmpPulse.department.internal;

import com.oman.EmpPulse.defaulthours.api.WeekSchedule;
import com.oman.EmpPulse.department.api.Department;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
  boolean existsByName(String name);

  boolean existsByIsDefaultTrue();

  @Query(
      "select distinct ws from WeekSchedule ws where ws.id in (select d.weekScheduleId from"
          + " Department d where d.id in :departmentIds)")
  List<WeekSchedule> findWeekScheduleByDepartmentIds(
      @Param("departmentIds") Collection<Long> departmentIds);
}
