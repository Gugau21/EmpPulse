package com.oman.EmpPulse.defaulthours.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, Long> {

  List<ScheduleBlock> findAllBySetIdOrderByDayOfWeekAsc(Long setId);

  Optional<ScheduleBlock> findBySetIdAndDayOfWeek(Long setId, int dayOfWeek);

  /**
   * Deletes every block of a schedule as a single bulk DELETE that runs immediately. This must not
   * be a derived {@code deleteAllBySetId}: that variant defers its row removals until flush, which
   * Hibernate orders <em>after</em> the new inserts in {@link DefaultHoursService#replaceSchedule}
   * — so replacing a schedule re-inserts the same {@code (set_id, day_of_week)} pair before the old
   * row is gone and trips the table's UNIQUE (set_id, day_of_week) constraint. Running the delete
   * eagerly clears those rows first; {@code clearAutomatically} drops any now-stale blocks from the
   * persistence context.
   */
  @Modifying(clearAutomatically = true)
  @Query("delete from ScheduleBlock b where b.setId = :setId")
  void deleteAllBySetId(@Param("setId") Long setId);
}
