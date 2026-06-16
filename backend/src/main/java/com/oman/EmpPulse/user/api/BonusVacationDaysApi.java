package com.oman.EmpPulse.user.api;

import java.util.Collection;
import java.util.List;
import com.oman.EmpPulse.user.internal.BonusVacationDays;


public interface BonusVacationDaysApi {
    List<BonusVacationDays> findByEmployeeIdIn(Collection<Long> employeeIds);
}
