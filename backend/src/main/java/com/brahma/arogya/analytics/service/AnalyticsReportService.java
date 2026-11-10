package com.brahma.arogya.analytics.service;

import com.brahma.arogya.analytics.dto.AnalyticsReportQualityRequest;
import com.brahma.arogya.analytics.repository.AnalyticsReportJdbcRepository;
import com.brahma.arogya.document.service.ClinicalDocumentService;
import java.io.*;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.*;
import java.util.*;
import java.util.zip.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class AnalyticsReportService {

  private final AnalyticsReportJdbcRepository db;

  public AnalyticsReportService(AnalyticsReportJdbcRepository db) {
    this.db = db;
  }

  public Map<String, Object> overview(String month, String department) {
    LocalDate start;
    try {
      start = YearMonth.parse(month).atDay(1);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use YYYY-MM");
    }
    Map<String, Object> now = metrics(start, start.plusMonths(1), department),
        before = metrics(start.minusMonths(1), start, department);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("month", month);
    out.put("current", now);
    out.put("previous", before);
    Map<String, Object> changes = new LinkedHashMap<>();
    for (String key : now.keySet()) {
      if (now.get(key) instanceof Number n && before.get(key) instanceof Number p) {
        BigDecimal current = new BigDecimal(n.toString()), prior = new BigDecimal(p.toString());
        Map<String, Object> delta = new LinkedHashMap<>();
        delta.put("absolute", current.subtract(prior));
        delta.put(
            "percent",
            prior.signum() == 0
                ? null
                : current
                    .subtract(prior)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(prior, 2, RoundingMode.HALF_UP));
        delta.put(
            "direction",
            current.compareTo(prior) > 0 ? "UP" : current.compareTo(prior) < 0 ? "DOWN" : "FLAT");
        changes.put(key, delta);
      }
    }
    out.put("changes", changes);
    out.put("departments", db.countEncountersForOverview(start, start.plusMonths(1)));
    out.put("trend", db.findPaymentsForOverview(start, start.plusMonths(1)));
    out.put("dummyPaymentsIncluded", false);
    return out;
  }

  private Map<String, Object> metrics(LocalDate start, LocalDate end, String department) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("patients", db.countPatientsForMetrics(start, end));
    m.put("visits", db.countEncountersForMetrics(start, end, department, department));
    m.put("averageWaitMinutes", db.readDepartmentsForMetrics(start, end, department, department));
    BigDecimal collected = db.readPaymentsForMetrics(start, end, department, department),
        refunds = db.readRefundsForMetrics(start, end, department, department);
    m.put("invoicedRevenue", db.readInvoicesForMetrics(start, end, department, department));
    m.put("missedMedicationDoses", db.countMedicationAdministrationsForMetrics(start, end));
    m.put("collections", collected);
    m.put("refunds", refunds);
    m.put("netCollections", collected.subtract(refunds));
    m.put("qualityEvents", db.countCareQualityEventsForMetrics(start, end, department, department));
    m.put("admissions", db.countAdmissionsForMetrics(start, end));
    return m;
  }

  public void quality(AnalyticsReportQualityRequest f, Principal p) {
    if (f.department() == null
        || f.eventType() == null
        || f.severity() == null
        || !Set.of("LOW", "MODERATE", "HIGH", "CRITICAL").contains(f.severity())
        || f.description() == null
        || f.description().isBlank())
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Department, event, severity and description required");
    db.insertCareQualityEventsForQuality(
        f.encounterId(), f.department(), f.eventType(), f.severity(), f.description(), p.getName());
  }

  public ResponseEntity<byte[]> xlsx(String month, String department) throws Exception {
    var r = overview(month, department);
    @SuppressWarnings("unchecked")
    var metrics = (Map<String, Object>) r.get("current");
    List<List<Object>> rows = new ArrayList<>();
    rows.add(List.of("Arogya monthly report", month, department));
    rows.add(List.of("Metric", "Current", "Previous", "Absolute change", "Percent change"));
    @SuppressWarnings("unchecked")
    var previous = (Map<String, Object>) r.get("previous");
    @SuppressWarnings("unchecked")
    var changes = (Map<String, Map<String, Object>>) r.get("changes");
    for (String key : metrics.keySet())
      rows.add(
          Arrays.asList(
              key,
              metrics.get(key),
              previous.get(key),
              changes.get(key).get("absolute"),
              changes.get(key).get("percent")));
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=arogya-" + month + ".xlsx")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(workbook(rows));
  }

  public ResponseEntity<byte[]> pdf(String month, String department) throws Exception {
    var r = overview(month, department);
    StringBuilder text =
        new StringBuilder(
            "Arogya Hospital\nMonthly performance report\nPeriod: "
                + month
                + " | Department: "
                + department
                + "\n");
    @SuppressWarnings("unchecked")
    var metrics = (Map<String, Object>) r.get("current");
    metrics.forEach((k, v) -> text.append(k).append(": ").append(v).append('\n'));
    text.append("Demo online payments excluded. Net collections deduct refunds.");
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=arogya-" + month + ".pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(ClinicalDocumentService.render(text.toString(), "Arogya analytics " + month));
  }

  public static byte[] workbook(List<List<Object>> rows) throws IOException {
    StringBuilder sheet =
        new StringBuilder(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet"
                + " xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
    int row = 1;
    for (var values : rows) {
      sheet.append("<row r=\"").append(row++).append("\">");
      for (Object v : values) {
        if (v instanceof Number) sheet.append("<c><v>").append(v).append("</v></c>");
        else
          sheet
              .append("<c t=\"inlineStr\"><is><t>")
              .append(escape(Objects.toString(v, "N/A")))
              .append("</t></is></c>");
      }
      sheet.append("</row>");
    }
    sheet.append("</sheetData></worksheet>");
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(out)) {
      entry(
          zip,
          "[Content_Types].xml",
          "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default"
              + " Extension=\"rels\""
              + " ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default"
              + " Extension=\"xml\" ContentType=\"application/xml\"/><Override"
              + " PartName=\"/xl/workbook.xml\""
              + " ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override"
              + " PartName=\"/xl/worksheets/sheet1.xml\""
              + " ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");
      entry(
          zip,
          "_rels/.rels",
          "<Relationships"
              + " xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship"
              + " Id=\"rId1\""
              + " Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\""
              + " Target=\"xl/workbook.xml\"/></Relationships>");
      entry(
          zip,
          "xl/workbook.xml",
          "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\""
              + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet"
              + " name=\"Monthly report\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
      entry(
          zip,
          "xl/_rels/workbook.xml.rels",
          "<Relationships"
              + " xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship"
              + " Id=\"rId1\""
              + " Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\""
              + " Target=\"worksheets/sheet1.xml\"/></Relationships>");
      entry(zip, "xl/worksheets/sheet1.xml", sheet.toString());
      zip.finish();
      return out.toByteArray();
    }
  }

  private static String escape(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private static void entry(ZipOutputStream z, String path, String text) throws IOException {
    z.putNextEntry(new ZipEntry(path));
    z.write(text.getBytes(StandardCharsets.UTF_8));
    z.closeEntry();
  }
}
