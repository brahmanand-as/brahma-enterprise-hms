package com.brahma.arogya.document.service;

import com.brahma.arogya.appointment.service.SchedulingService;
import com.brahma.arogya.document.dto.ClinicalDocumentIssueRequest;
import com.brahma.arogya.document.repository.ClinicalDocumentJdbcRepository;
import com.brahma.arogya.security.service.CareScope;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.*;
import java.security.Principal;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class ClinicalDocumentService {

  private final ClinicalDocumentJdbcRepository db;

  private final CareScope care;

  private final SchedulingService scheduling;

  public ClinicalDocumentService(
      ClinicalDocumentJdbcRepository db, CareScope care, SchedulingService scheduling) {
    this.db = db;
    this.care = care;
    this.scheduling = scheduling;
  }

  public List<Map<String, Object>> documents(
      Principal p, jakarta.servlet.http.HttpServletRequest req) {
    return req.getRequestURI().contains("/patient/")
        ? db.findIssuedDocumentsForDocuments(scheduling.patient(p))
        : db.findIssuedDocumentsForDocuments2();
  }

  @Transactional
  public Map<String, Object> issue(ClinicalDocumentIssueRequest f, Principal principal) {
    if (f.type() == null
        || f.referenceId() == null
        || !Set.of("PRESCRIPTION", "LAB_REPORT", "RADIOLOGY_REPORT").contains(f.type()))
      throw bad("Final document type and reference required");
    var existing = db.findIssuedDocumentsForIssue(f.type(), f.referenceId());
    if (!existing.isEmpty()) {
      care.require(((Number) existing.get(0).get("patient_id")).longValue());
      return Map.of("id", existing.get(0).get("id"));
    }
    List<String> lines = new ArrayList<>();
    long patient;
    if (f.type().equals("PRESCRIPTION")) {
      var r = db.findPrescriptionsForIssue(f.referenceId());
      patient = ((Number) r.get("patient_id")).longValue();
      lines.add("Prescribing clinician: " + r.get("doctor_name"));
      for (var i : db.findPrescriptionItemsForIssue(f.referenceId())) {
        lines.add(String.valueOf(i.get("medicine_name")));
        lines.add(
            "Dose: "
                + i.get("dose")
                + " | Frequency: "
                + i.get("frequency")
                + " | Duration: "
                + i.get("duration"));
        if (i.get("instructions") != null) lines.add("Instructions: " + i.get("instructions"));
      }
    } else if (f.type().equals("LAB_REPORT")) {
      var r = db.findLabOrderItemsForIssue(f.referenceId());
      patient = ((Number) r.get("patient_id")).longValue();
      lines.add(String.valueOf(r.get("test_name")));
      lines.add(
          "Result: " + r.get("result_value") + " " + Objects.toString(r.get("result_unit"), ""));
      lines.add("Reference range: " + Objects.toString(r.get("reference_range"), "Not supplied"));
      lines.add("Verified by: " + r.get("verified_by"));
      lines.add("Verified at: " + r.get("verified_at"));
      lines.add("Specimen: " + Objects.toString(r.get("sample_barcode"), "Not supplied"));
    } else {
      var r = db.findRadiologyOrdersForIssue(f.referenceId());
      patient = ((Number) r.get("patient_id")).longValue();
      lines.add("Study: " + r.get("study_name"));
      lines.add("Report: " + r.get("report"));
      lines.add("Verified by: " + r.get("verified_by"));
      lines.add("Verified at: " + r.get("verified_at"));
    }
    care.require(patient);
    var p = db.findPatientsForIssue(patient);
    String hospital = db.readHospitalSettingsForIssue();
    lines.addFirst(
        "MRN: "
            + p.get("uhid")
            + " | Patient: "
            + p.get("first_name")
            + " "
            + Objects.toString(p.get("last_name"), ""));
    lines.addFirst(f.type().replace('_', ' '));
    lines.addFirst(hospital);
    lines.add(
        "Issued by: "
            + principal.getName()
            + " | "
            + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
    String token = UUID.randomUUID().toString();
    String snapshot = String.join("\n", lines);
    db.insertIssuedDocumentsForIssue(
        patient, f.type(), f.referenceId(), token, snapshot, principal.getName());
    return Map.of("id", db.readGeneratedIdForIssue(), "status", "ISSUED");
  }

  public ResponseEntity<byte[]> pdf(
      long id, Principal p, jakarta.servlet.http.HttpServletRequest req) throws Exception {
    var d = db.findIssuedDocumentsForPdf(id);
    if (req.getRequestURI().contains("/patient/")
        && ((Number) d.get("patient_id")).longValue() != scheduling.patient(p))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    care.require(((Number) d.get("patient_id")).longValue());
    String verify =
        req.getScheme()
            + "://"
            + req.getServerName()
            + ((req.getServerPort() == 80 || req.getServerPort() == 443)
                ? ""
                : ":" + req.getServerPort())
            + "/api/public/documents/verify/"
            + d.get("verification_token");
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=arogya-" + id + ".pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(render(String.valueOf(d.get("content_text")), verify));
  }

  public Map<String, Object> verify(String token) {
    return db.findIssuedDocumentsForVerify(token);
  }

  public static byte[] render(String content, String verificationUrl) throws Exception {
    try (PDDocument document = new PDDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream fontFile =
            ClinicalDocumentService.class.getResourceAsStream("/fonts/DejaVuSans.ttf")) {
      PDFont font =
          PDType0Font.load(
              document, Objects.requireNonNull(fontFile, "Bundled report font missing"));
      String[] paragraphs = content.split("\\R");
      String hospital = paragraphs.length > 0 ? paragraphs[0] : "Arogya Hospital",
          title = paragraphs.length > 1 ? paragraphs[1] : "Verified document",
          patient = paragraphs.length > 2 ? paragraphs[2] : "";
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);
      PDPageContentStream stream = new PDPageContentStream(document, page);
      documentHeader(stream, font, hospital, title, patient);
      float y = 660;
      for (int index = 3; index < paragraphs.length; index++) {
        for (String line : wrap(paragraphs[index], 78)) {
          if (y < 170) {
            stream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            documentHeader(stream, font, hospital, title, patient);
            y = 660;
          }
          text(stream, font, 10.5f, 48, y, line);
          y -= 18;
        }
        y -= 12;
      }
      stream.setStrokingColor(0.82f, 0.88f, 0.9f);
      stream.moveTo(48, 142);
      stream.lineTo(547, 142);
      stream.stroke();
      boolean verifiable =
          verificationUrl.startsWith("http://") || verificationUrl.startsWith("https://");
      if (verifiable) {
        BitMatrix qr = new QRCodeWriter().encode(verificationUrl, BarcodeFormat.QR_CODE, 86, 86);
        stream.setNonStrokingColor(0f, 0f, 0f);
        for (int row = 0; row < qr.getHeight(); row++)
          for (int col = 0; col < qr.getWidth(); col++)
            if (qr.get(col, row)) {
              stream.addRect(48 + col, 35 + 86 - row, 1, 1);
              stream.fill();
            }
        text(stream, font, 9, 153, 106, "ISSUED & VERIFIED");
        text(stream, font, 8, 153, 87, "Scan the QR code to verify this document.");
        text(stream, font, 8, 153, 70, "Draft clinical documents are never issued.");
      } else {
        text(stream, font, 9, 48, 108, "MANAGEMENT REPORT");
        text(
            stream,
            font,
            8,
            48,
            88,
            "Generated from operational ledgers. Simulated payments are excluded.");
      }
      stream.close();
      for (int n = 0; n < document.getNumberOfPages(); n++) {
        try (var footer =
            new PDPageContentStream(
                document, document.getPage(n), PDPageContentStream.AppendMode.APPEND, true)) {
          text(footer, font, 7, 440, 20, "Page " + (n + 1) + " of " + document.getNumberOfPages());
        }
      }
      document.save(out);
      return out.toByteArray();
    }
  }

  private static void documentHeader(
      PDPageContentStream s, PDFont font, String hospital, String title, String patient)
      throws IOException {
    s.setNonStrokingColor(0.06f, 0.17f, 0.26f);
    s.addRect(0, 724, 595, 118);
    s.fill();
    s.setNonStrokingColor(1f, 1f, 1f);
    text(s, font, 20, 48, 788, hospital);
    s.setNonStrokingColor(0.43f, 0.85f, 0.77f);
    text(s, font, 11, 48, 759, title);
    s.setNonStrokingColor(0.92f, 0.97f, 0.96f);
    s.addRect(38, 681, 519, 32);
    s.fill();
    s.setNonStrokingColor(0.1f, 0.24f, 0.3f);
    text(s, font, 9, 48, 693, patient);
  }

  private static void text(
      PDPageContentStream s, PDFont font, float size, float x, float y, String line)
      throws IOException {
    StringBuilder safe = new StringBuilder();
    for (int c : line.codePoints().toArray()) {
      String character = new String(Character.toChars(c));
      try {
        font.encode(character);
        safe.append(character);
      } catch (IllegalArgumentException e) {
        safe.append('?');
      }
    }
    s.beginText();
    s.setFont(font, size);
    s.newLineAtOffset(x, y);
    s.showText(safe.toString());
    s.endText();
  }

  public static List<String> wrap(String text, int width) {
    List<String> lines = new ArrayList<>();
    while (text.length() > width) {
      int at = text.lastIndexOf(' ', width);
      if (at < 1) at = width;
      lines.add(text.substring(0, at));
      text = text.substring(at).stripLeading();
    }
    lines.add(text);
    return lines;
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }
}
