package com.oman.EmpPulse.export.internal;

import com.oman.EmpPulse.department.api.Department;
import com.oman.EmpPulse.user.internal.Employee;
import com.oman.EmpPulse.user.internal.User;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvExportService {

    public byte[] departmentsToCsv(List<Department> departments) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter printer = new CSVPrinter(
                 new OutputStreamWriter(out, StandardCharsets.UTF_8),
                 CSVFormat.DEFAULT.builder()
                     .setHeader("ID", "Name", "Week Schedule ID")
                     .build())) {

            for (Department d : departments) {
                printer.printRecord(d.getId(), d.getName(), d.getWeekScheduleId());
            }

            printer.flush();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }

    public byte[] usersToCsv(List<User> users) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                    .setHeader("ID", "Name", "Surname", "Email", "Theme", "Language", "Is Owner", "Active")
                    .build())) {

            for (User u : users) {
                printer.printRecord(
                    u.getId(),
                    u.getName(),
                    u.getSurname(),
                    u.getEmail(),
                    u.getTheme(),
                    u.getLanguage(),
                    u.isOwner(),
                    u.isActive()
                );
            }

            printer.flush();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV for users", e);
        }
    }

    public byte[] employeesToCsv(List<Employee> employees) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
         CSVPrinter printer = new CSVPrinter(
             new OutputStreamWriter(out, StandardCharsets.UTF_8),
             CSVFormat.DEFAULT.builder()
                 .setHeader("ID", "Department ID", "Week Schedule ID", "Vacation Balance", "Active")
                 .build())) {

        for (Employee e : employees) {
            printer.printRecord(
                e.getId(),
                e.getDepartmentId(),
                e.getWeekScheduleId(),
                e.getVacationBalance(),
                e.isActive()
            );
        }

        printer.flush();
        return out.toByteArray();

    } catch (IOException e) {
        throw new RuntimeException("Failed to generate CSV for employees", e);
    }
}

}