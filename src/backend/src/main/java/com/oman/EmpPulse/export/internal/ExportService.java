package com.oman.EmpPulse.export.internal;

import com.oman.EmpPulse.defaulthours.api.DefaultHoursApi;
import com.oman.EmpPulse.defaulthours.api.ScheduleBlock;
import com.oman.EmpPulse.defaulthours.api.WeekSchedule;
import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.department.api.DepartmentApi;
import com.oman.EmpPulse.leave.api.Leave;
import com.oman.EmpPulse.leave.api.LeaveApi;
import com.oman.EmpPulse.loggedhours.api.LoggedHours;
import com.oman.EmpPulse.loggedhours.api.LoggedHoursApi;
import com.oman.EmpPulse.shared.security.AuthUtils;
import com.oman.EmpPulse.user.api.Admin;
import com.oman.EmpPulse.user.api.AdminApi;
import com.oman.EmpPulse.user.api.BonusVacationDays;
import com.oman.EmpPulse.user.api.BonusVacationDaysApi;
import com.oman.EmpPulse.user.api.Employee;
import com.oman.EmpPulse.user.api.EmployeeApi;
import com.oman.EmpPulse.user.api.User;
import com.oman.EmpPulse.user.api.UserApi;
import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

  private final DepartmentApi departmentApi;
  private final CsvExportService csvExportService;
  private final ZipService zipService;
  private final AdminApi adminApi;
  private final EmployeeApi employeeApi;
  private final UserApi userApi;
  private final LeaveApi leaveApi;
  private final LoggedHoursApi loggedHoursApi;
  private final BonusVacationDaysApi bonusVacationDaysApi;
  private final DefaultHoursApi defaultHoursApi;

  public ExportService(
      DepartmentApi departmentApi,
      AdminApi adminApi,
      CsvExportService csvExportService,
      ZipService zipService,
      EmployeeApi employeeApi,
      UserApi userApi,
      LeaveApi leaveApi,
      LoggedHoursApi loggedHoursApi,
      BonusVacationDaysApi bonusVacationDaysApi,
      DefaultHoursApi defaultHoursApi) {
    this.departmentApi = departmentApi;
    this.adminApi = adminApi;
    this.csvExportService = csvExportService;
    this.zipService = zipService;
    this.employeeApi = employeeApi;
    this.userApi = userApi;
    this.leaveApi = leaveApi;
    this.loggedHoursApi = loggedHoursApi;
    this.bonusVacationDaysApi = bonusVacationDaysApi;
    this.defaultHoursApi = defaultHoursApi;
  }

  public byte[] export(Authentication authentication) {
    List<Department> departments =
        isOwner(authentication)
            ? new ArrayList<Department>(departmentApi.findAll())
            : new ArrayList<Department>(
                departmentApi.findAllByIds(
                    adminApi.departmentIdsForAdminUser(AuthUtils.getUserId(authentication))));
    List<Employee> employees =
        employeeApi
            .findAllByDepartmentIdIn(
                departments.stream().map(Department::getId).collect(Collectors.toSet()))
            .stream()
            .toList();
    Set<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toSet());
    List<Leave> leaves = leaveApi.findAllByEmployeeIdIn(employeeIds).stream().toList();
    List<Admin> admins =
        adminApi
            .findAllByDepartmentIdIn(
                departments.stream().map(Department::getId).collect(Collectors.toList()))
            .stream()
            .toList();
    Set<User> users = userApi.findByIdIn(employeeIds).stream().collect(Collectors.toSet());
    users.addAll(userApi.findByIdIn(admins.stream().map(Admin::getId).collect(Collectors.toSet())));
    users.add(userApi.findById(AuthUtils.getUserId(authentication)).orElseThrow());
    List<LoggedHours> loggedHours =
        loggedHoursApi.findAllByEmployeeIdIn(employeeIds).stream().toList();
    List<BonusVacationDays> bonusVacationDays =
        bonusVacationDaysApi.findByEmployeeIdIn(employeeIds).stream().toList();
    Set<WeekSchedule> weekSchedules =
        departmentApi
            .findWeekScheduleByDepartmentIds(
                departments.stream().map(Department::getId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toSet());
    weekSchedules.addAll(
        employeeApi.findWeekScheduleByEmployeeIds(employeeIds).stream()
            .collect(Collectors.toSet()));
    List<ScheduleBlock> scheduleBlocks =
        defaultHoursApi
            .findEmployeeScheduleBlocks(
                weekSchedules.stream().map(WeekSchedule::getId).collect(Collectors.toSet()))
            .stream()
            .toList();
    List<CsvExportService.MonthlyWorkHours> monthlyWorkHoursReport =
        buildMonthlyWorkHoursReport(loggedHours, users);

    Map<String, byte[]> files = new HashMap<>();
    files.put("departments.csv", csvExportService.departmentsToCsv(departments));
    files.put("employees.csv", csvExportService.employeesToCsv(employees));
    files.put("users.csv", csvExportService.usersToCsv(users));
    files.put("leaves.csv", csvExportService.leavesToCsv(leaves));
    files.put("logged_hours.csv", csvExportService.loggedHoursToCsv(loggedHours));
    files.put(
        "monthly_work_hours.csv", csvExportService.monthlyWorkHoursToCsv(monthlyWorkHoursReport));
    files.put(
        "bonus_vacation_days.csv", csvExportService.bonusVacationDaysToCsv(bonusVacationDays));
    files.put("week_schedules.csv", csvExportService.weekSchedulesToCsv(weekSchedules));
    files.put("schedule_blocks.csv", csvExportService.scheduleBlocksToCsv(scheduleBlocks));
    return zipService.zip(files);
  }

  private List<CsvExportService.MonthlyWorkHours> buildMonthlyWorkHoursReport(
      List<LoggedHours> loggedHours, Set<User> users) {
    Map<Long, User> userById = users.stream().collect(Collectors.toMap(User::getId, user -> user));

    return loggedHours.stream()
        .collect(
            Collectors.groupingBy(
                lh -> new MonthEmployeeKey(lh.getEmployeeId(), YearMonth.from(lh.getDate())),
                Collectors.summingDouble(
                    lh -> Duration.between(lh.getStartTime(), lh.getEndTime()).toMinutes() / 60.0)))
        .entrySet()
        .stream()
        .map(
            entry -> {
              MonthEmployeeKey key = entry.getKey();
              User user = userById.get(key.employeeId());
              return new CsvExportService.MonthlyWorkHours(
                  key.employeeId(),
                  user != null ? user.getName() : "",
                  user != null ? user.getSurname() : "",
                  key.yearMonth().getYear(),
                  key.yearMonth().getMonthValue(),
                  entry.getValue());
            })
        .sorted(
            Comparator.comparing(CsvExportService.MonthlyWorkHours::employeeId)
                .thenComparing(CsvExportService.MonthlyWorkHours::year)
                .thenComparing(CsvExportService.MonthlyWorkHours::month))
        .toList();
  }

  private record MonthEmployeeKey(Long employeeId, YearMonth yearMonth) {}

  private boolean isOwner(Authentication authentication) {
    return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("OWNER"));
  }
}
