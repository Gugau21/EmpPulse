package com.oman.EmpPulse.export.internal;

import com.oman.EmpPulse.defaulthours.internal.ScheduleBlock;
import com.oman.EmpPulse.defaulthours.internal.WeekSchedule;
import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.leave.internal.Leave;
import com.oman.EmpPulse.loggedhours.internal.LoggedHours;
import com.oman.EmpPulse.user.internal.BonusVacationDays;
import com.oman.EmpPulse.user.internal.Employee;
import com.oman.EmpPulse.user.internal.User;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

@Service
public class CsvExportService {
  public static record MonthlyWorkHours(
      Long employeeId,
      String employeeName,
      String employeeSurname,
      int year,
      int month,
      double totalHours) {}

  public byte[] departmentsToCsv(Iterable<Department> departments) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer =
            new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader("ID", "Name", "Week Schedule ID").build())) {

      for (Department d : departments) {
        printer.printRecord(d.getId(), d.getName(), d.getWeekScheduleId());
      }

      printer.flush();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate CSV", e);
    }
  }

  public byte[] usersToCsv(Iterable<User> users) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer =
            new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader("ID", "Name", "Surname", "Email", "Active")
                    .build())) {

      for (User u : users) {
        printer.printRecord(u.getId(), u.getName(), u.getSurname(), u.getEmail(), u.isActive());
      }

      printer.flush();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate CSV for users", e);
    }
  }

  public byte[] employeesToCsv(Iterable<Employee> employees) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer =
            new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader(
                        "ID", "Department ID", "Week Schedule ID", "Vacation Balance", "Active")
                    .build())) {

      for (Employee e : employees) {
        printer.printRecord(
            e.getId(),
            e.getDepartmentId(),
            e.getWeekScheduleId(),
            e.getVacationBalance(),
            e.isActive());
      }

      printer.flush();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate CSV for employees", e);
    }
  }

  public byte[] leavesToCsv(Iterable<Leave> leaves) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer =
            new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader(
                        "ID",
                        "Employee ID",
                        "Type",
                        "Start Date",
                        "End Date",
                        "Paid",
                        "Status",
                        "Description",
                        "Admin Reviewer ID",
                        "Admin Comment",
                        "Modification ID",
                        "Created At",
                        "Updated At")
                    .build())) {

      for (Leave l : leaves) {
        printer.printRecord(
            l.getId(),
            l.getEmployeeId(),
            l.getType(),
            l.getStartDate(),
            l.getEndDate(),
            l.isPaid(),
            l.getStatus(),
            l.getDescription(),
            l.getAdminReviewerId(),
            l.getAdminComment(),
            l.getModificationId(),
            l.getCreatedAt(),
            l.getUpdatedAt());
      }

      printer.flush();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate CSV for leaves", e);
    }
  }

  public byte[] loggedHoursToCsv(Iterable<LoggedHours> hoursList) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer =
            new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader("ID", "Employee ID", "Admin ID", "Date", "Start Time", "End Time")
                    .build())) {

      for (LoggedHours lh : hoursList) {
        printer.printRecord(
            lh.getId(),
            lh.getEmployeeId(),
            lh.getAdminId(),
            lh.getDate(),
            lh.getStartTime(),
            lh.getEndTime());
      }

      printer.flush();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate CSV for logged hours", e);
    }
  }

  public byte[] monthlyWorkHoursToCsv(Iterable<MonthlyWorkHours> reportRows) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer =
            new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader(
                        "Employee ID",
                        "Employee Name",
                        "Employee Surname",
                        "Year",
                        "Month",
                        "Total Hours")
                    .build())) {

      for (MonthlyWorkHours row : reportRows) {
        printer.printRecord(
            row.employeeId(),
            row.employeeName(),
            row.employeeSurname(),
            row.year(),
            row.month(),
            String.format(Locale.ROOT, "%.2f", row.totalHours()));
      }

      printer.flush();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate CSV for monthly work hours", e);
    }
  }

  public byte[] bonusVacationDaysToCsv(Iterable<BonusVacationDays> bonusDaysList) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer =
            new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader("ID", "Employee ID", "Year", "Days")
                    .build())) {

      for (BonusVacationDays bvd : bonusDaysList) {
        printer.printRecord(bvd.getId(), bvd.getEmployeeId(), bvd.getYear(), bvd.getDays());
      }

      printer.flush();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate CSV for bonus vacation days", e);
    }
  }

  public byte[] scheduleBlocksToCsv(Iterable<ScheduleBlock> blocks) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer =
            new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader("ID", "Set ID", "Day Of Week", "Start Time", "End Time")
                    .build())) {

      for (ScheduleBlock sb : blocks) {
        printer.printRecord(
            sb.getId(), sb.getSetId(), sb.getDayOfWeek(), sb.getStartTime(), sb.getEndTime());
      }

      printer.flush();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate CSV for schedule blocks", e);
    }
  }

  public byte[] weekSchedulesToCsv(Iterable<WeekSchedule> schedules) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer =
            new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader("ID").build())) {

      for (WeekSchedule ws : schedules) {
        printer.printRecord(ws.getId());
      }

      printer.flush();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate CSV for week schedules", e);
    }
  }
}
