package com.oman.EmpPulse.leave.internal;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

  /**
   * Checks whether the employee has an active (pending or approved) leave whose date range overlaps
   * [startDate, endDate].
   */
  @Query(
      value =
          """
          select exists(
              select 1 from leave
              where employee_id = :employeeId
                and status in ('pending', 'approved')
                and start_date <= :endDate
                and end_date >= :startDate)
          """,
      nativeQuery = true)
  boolean existsActiveOverlap(
      @Param("employeeId") Long employeeId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
