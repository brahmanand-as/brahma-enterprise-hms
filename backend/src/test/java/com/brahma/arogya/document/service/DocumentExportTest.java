package com.brahma.arogya.document.service;

import static org.junit.jupiter.api.Assertions.*;

import com.brahma.arogya.analytics.service.AnalyticsReportService;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

class DocumentExportTest {
  @Test
  void longIssuedDocumentsPaginateWithoutLosingText() throws Exception {
    String text =
        "Arogya Hospital\nMRN-00000001\n"
            + "Synthetic clinical line\n".repeat(160)
            + "FINAL VERIFIED RESULT";
    byte[] bytes = ClinicalDocumentService.render(text, "https://example.test/verify/synthetic");
    try (var doc = Loader.loadPDF(bytes)) {
      assertTrue(doc.getNumberOfPages() > 1);
      String extracted = new PDFTextStripper().getText(doc);
      assertTrue(extracted.contains("MRN-00000001"));
      assertTrue(extracted.contains("FINAL VERIFIED RESULT"));
    }
  }

  @Test
  void xlsxContainsWorkbookRelationsAndNumericCells() throws Exception {
    byte[] bytes =
        AnalyticsReportService.workbook(
            List.of(
                List.of("Metric", "Value"),
                List.of("Patients", 12),
                List.of("Name with < and &", 3)));
    Map<String, String> parts = new HashMap<>();
    try (var zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
      ZipEntry e;
      while ((e = zip.getNextEntry()) != null)
        parts.put(
            e.getName(), new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
    }
    assertTrue(parts.containsKey("[Content_Types].xml"));
    assertTrue(parts.get("xl/worksheets/sheet1.xml").contains("<v>12</v>"));
    assertTrue(parts.get("xl/worksheets/sheet1.xml").contains("&lt; and &amp;"));
  }
}
