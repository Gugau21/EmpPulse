package com.oman.EmpPulse.bootstrap;

import com.oman.EmpPulse.department.api.DepartmentApi;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DefaultDepartmentSeeder implements CommandLineRunner {

  private final DepartmentApi departmentApi;

  public DefaultDepartmentSeeder(DepartmentApi departmentApi) {
    this.departmentApi = departmentApi;
  }

  @Override
  public void run(String... args) {
    departmentApi.ensureDefaultDepartmentExists();
  }
}
