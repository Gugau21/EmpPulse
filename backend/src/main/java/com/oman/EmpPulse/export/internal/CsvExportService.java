package com.oman.EmpPulse.export.internal;

import com.oman.EmpPulse.department.api.Department;
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

    public byte[] toCsv(List<Department> departments) {
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
}