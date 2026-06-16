package com.oman.EmpPulse.user.internal;

import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BonusVacationDaysService implements com.oman.EmpPulse.user.api.BonusVacationDaysApi {

  private final BonusVacationDaysRepository repository;

  public BonusVacationDaysService(BonusVacationDaysRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<BonusVacationDays> findByEmployeeIdIn(Collection<Long> employeeIds) {
    return repository.findByEmployeeIdIn(employeeIds);
  }
    
}
