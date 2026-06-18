package com.oman.EmpPulse.user.api;

import com.oman.EmpPulse.user.internal.BonusVacationDays;
import java.util.Collection;
import java.util.List;

public interface BonusVacationDaysApi {
  List<BonusVacationDays> findByEmployeeIdIn(Collection<Long> employeeIds);
}
