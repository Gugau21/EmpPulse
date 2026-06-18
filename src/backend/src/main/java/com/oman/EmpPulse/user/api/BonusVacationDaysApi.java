package com.oman.EmpPulse.user.api;

import java.util.Collection;
import java.util.List;

public interface BonusVacationDaysApi {
  List<BonusVacationDays> findByEmployeeIdIn(Collection<Long> employeeIds);
}
