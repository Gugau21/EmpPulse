package com.oman.EmpPulse.leave.internal;

import com.oman.EmpPulse.leave.api.Leave;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

  /**
   * Same as {@link #existsActiveOverlap}, but ignores the leaves identified by {@code excludeIds}.
   */
  @Query(
      value =
          """
          select exists(
              select 1 from leave
              where employee_id = :employeeId
                and id not in (:excludeIds)
                and status in ('pending', 'approved')
                and start_date <= :endDate
                and end_date >= :startDate)
          """,
      nativeQuery = true)
  boolean existsActiveOverlapExcludingIds(
      @Param("employeeId") Long employeeId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("excludeIds") Collection<Long> excludeIds);

  /** Whether a pending modification request is currently linked to the given original leave. */
  boolean existsByModificationId(Long modificationId);

  /** The (at most one) pending modification request linked to the given original leave. */
  Optional<Leave> findByModificationId(Long modificationId);

  List<Leave> findAllByEmployeeIdOrderByUpdatedAtDesc(Long employeeId);

  List<Leave> findAllByEmployeeIdInOrderByUpdatedAtDesc(Collection<Long> employeeIds);

  /**
   * Finds the leaves visible to an admin: those of employees in the given departments, plus the
   * admin's own. Sorted by last change, newest first.
   *
   * <p>{@code departmentIds} must be non-empty (an empty list breaks the native {@code in} clause);
   * callers with no overseen departments use {@link #findAllByEmployeeIdOrderByUpdatedAtDesc}.
   */
  @Query(
      value =
          """
          select l.* from leave l
          join employee e on e.id = l.employee_id
          where l.employee_id = :callerId or e.department_id in (:departmentIds)
          order by l.updated_at desc
          """,
      nativeQuery = true)
  List<Leave> findVisibleToAdmin(
      @Param("callerId") Long callerId, @Param("departmentIds") Collection<Long> departmentIds);

  /**
   * Finds approved leaves currently in progress for the given employees ({@code startDate <= today
   * <= endDate}).
   */
  @Query(
      value =
          """
          select * from leave
          where employee_id in (:employeeIds)
            and status = 'approved'
            and start_date <= :today
            and end_date >= :today
          """,
      nativeQuery = true)
  List<Leave> findActiveApprovedByEmployeeIds(
      @Param("employeeIds") Collection<Long> employeeIds, @Param("today") LocalDate today);

  /**
   * Finds the employee's active (pending or approved) Vacation leaves whose date range overlaps
   * [yearStart, yearEnd], for counting used vacation days.
   *
   * <p>A pending modification request overrides the original it targets: the modification (with its
   * new dates) is counted, while its parent is excluded. So this excludes any leave that currently
   * has a live (pending or approved) modification pointing at it.
   */
  @Query(
      value =
          """
          select l.* from leave l
          where l.employee_id = :employeeId
            and l.type = 'vacation'
            and l.status in ('pending', 'approved')
            and l.start_date <= :yearEnd
            and l.end_date >= :yearStart
            and not exists (
                select 1 from leave m
                where m.modification_id = l.id
                  and m.status in ('pending', 'approved'))
          """,
      nativeQuery = true)
  List<Leave> findActiveVacationLeavesOverlapping(
      @Param("employeeId") Long employeeId,
      @Param("yearStart") LocalDate yearStart,
      @Param("yearEnd") LocalDate yearEnd);

  /** Finds employees from the given set who have an approved unpaid leave covering {@code date}. */
  @Query(
      value =
          """
          select employee_id from leave
          where employee_id in (:employeeIds)
            and status = 'approved'
            and paid = false
            and start_date <= :date
            and end_date >= :date
          """,
      nativeQuery = true)
  List<Long> findEmployeeIdsOnUnpaidApprovedLeave(
      @Param("employeeIds") Collection<Long> employeeIds, @Param("date") LocalDate date);
}
