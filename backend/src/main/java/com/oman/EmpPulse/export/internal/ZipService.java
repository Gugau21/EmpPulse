package com.oman.EmpPulse.export.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Service;

@Service
public class ZipService {

  public byte[] zip(Map<String, byte[]> files) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(out)) {

      for (Map.Entry<String, byte[]> entry : files.entrySet()) {
        zip.putNextEntry(new ZipEntry(entry.getKey()));
        zip.write(entry.getValue());
        zip.closeEntry();
      }

      zip.finish();
      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to create ZIP", e);
    }
  }
}
