package com.oman.EmpPulse.defaulthours.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import com.oman.EmpPulse.defaulthours.api.WeekSchedule;

public interface WeekScheduleRepository extends JpaRepository<WeekSchedule, Long> {}
