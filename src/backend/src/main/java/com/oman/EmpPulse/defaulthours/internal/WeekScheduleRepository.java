package com.oman.EmpPulse.defaulthours.internal;

import com.oman.EmpPulse.defaulthours.api.WeekSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeekScheduleRepository extends JpaRepository<WeekSchedule, Long> {}
