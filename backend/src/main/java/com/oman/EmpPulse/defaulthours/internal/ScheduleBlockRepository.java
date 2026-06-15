package com.oman.EmpPulse.defaulthours.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, Long> {

  List<ScheduleBlock> findAllBySetIdOrderByDayOfWeekAsc(Long setId);

  void deleteAllBySetId(Long setId);
}
