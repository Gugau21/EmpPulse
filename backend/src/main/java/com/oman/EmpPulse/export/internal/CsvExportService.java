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

@Service
public class CsvExportService {

    public byte[] departmentsToCsv(Iterable<Department> departments) {
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

    public byte[] usersToCsv(Iterable<User> users) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                    .setHeader("ID", "Name", "Surname", "Email", "Active")
                    .build())) {

            for (User u : users) {
                printer.printRecord(
                    u.getId(),
                    u.getName(),
                    u.getSurname(),
                    u.getEmail(),
                    u.isActive()
                );
            }

            printer.flush();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV for users", e);
        }
    }

    public byte[] employeesToCsv(Iterable<Employee> employees) {
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