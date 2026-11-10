package com.brahma.arogya.operations.service;

import com.brahma.arogya.billing.service.RevenueLedger;
import com.brahma.arogya.document.dto.ClinicalDocumentIssueRequest;
import com.brahma.arogya.document.service.ClinicalDocumentService;
import com.brahma.arogya.inpatient.service.BedChargeService;
import com.brahma.arogya.operations.dto.IntegratedOperationsAdmitRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsInvoiceLineRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsInvoiceRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsLabRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsLabStatusRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsPaymentRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsStockRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsTestRequest;
import com.brahma.arogya.operations.repository.IntegratedOperationsJdbcRepository;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class IntegratedOperationsService {

  private final RevenueLedger ledger;

  private final BedChargeService bedCharges;

  private final ClinicalDocumentService documents;

  private final IntegratedOperationsJdbcRepository db;

  public IntegratedOperationsService(
      IntegratedOperationsJdbcRepository db,
      RevenueLedger ledger,
      BedChargeService bedCharges,
      ClinicalDocumentService documents) {
    this.db = db;
    this.ledger = ledger;
    this.bedCharges = bedCharges;
    this.documents = documents;
  }

  private ResponseStatusException bad(String msg) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
  }

  private long required(Long n, String name) {
    if (n == null || n < 1) throw bad(name + " required");
    return n;
  }

  private BigDecimal positive(BigDecimal n, String name) {
    if (n == null || n.signum() <= 0 || n.scale() > 2 || n.precision() > 12)
      throw bad(name + " must be positive");
    return n;
  }

  private String id(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
  }

  private void audit(Principal p, String action, String type, Object id) {
    db.insertAuditLogsForAudit(p.getName(), action, type, String.valueOf(id));
  }

  private boolean exists(String table, long id) {
    return db.countGeneratedIdForExists(table, id) > 0;
  }

  private long newId() {
    return db.readGeneratedIdForNewId();
  }

  public List<Map<String, Object>> labItems() {
    return db.findLabOrderItemsForLabItems();
  }

  @Transactional
  public Map<String, Object> labOrder(IntegratedOperationsLabRequest req, Principal p) {
    long eid = required(req.encounterId(), "encounterId");
    if (req.tests() == null || req.tests().isEmpty()) throw bad("At least one test required");
    Map<String, Object> e = db.findEncountersForLabOrder(eid);
    if (!"IN_CONSULTATION".equals(e.get("status")))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Laboratory ordering requires an active consultation");
    String no = id("LAB");
    db.insertLabOrdersForLabOrder(no, eid, e.get("patient_id"), p.getName());
    long oid = newId();
    for (IntegratedOperationsTestRequest t : req.tests()) {
      Map<String, Object> cat = db.findLabTestCatalogForLabOrder(t.testCode());
      String processing =
          t.processingType() == null
              ? String.valueOf(cat.get("default_processing_type"))
              : t.processingType();
      if (!Set.of("IN_HOUSE", "OUTSOURCE").contains(processing))
        throw bad("Invalid processingType");
      if ("OUTSOURCE".equals(processing) && t.externalLabId() == null)
        throw bad("externalLabId required for outsource");
      db.insertLabOrderItemsForLabOrder(
          oid, t.testCode(), cat.get("name"), processing, t.externalLabId());
      long itemId = newId();
      long invoice =
          ledger.charge(
              ((Number) e.get("patient_id")).longValue(),
              eid,
              "LAB-" + itemId,
              "LAB",
              itemId,
              String.valueOf(cat.get("name")),
              BigDecimal.ONE,
              (BigDecimal) cat.get("price"));
      db.updateLabOrderItemsForLabOrder(
          invoice, cat.get("result_unit"), cat.get("reference_range"), itemId);
    }
    audit(p, "LAB_ORDER_CREATED", "LAB_ORDER", oid);
    return db.findLabOrdersForLabOrder(oid);
  }

  @Transactional
  public Map<String, Object> labTransition(
      long id, IntegratedOperationsLabStatusRequest r, Principal p) {
    Map<String, Object> item = db.findLabOrderItemsForLabTransition(id);
    String old = (String) item.get("status");
    var auth =
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication();
    boolean admin =
        auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (!admin) {
      String required =
          "VERIFIED".equals(r.status())
              ? "PERM_lab.verify"
              : "COMPLETED".equals(r.status()) ? "PERM_lab.complete" : "PERM_lab.process";
      if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals(required)))
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Laboratory transition requires " + required);
    }
    Map<String, String> next =
        Map.of(
            "ORDERED",
            "BILLED",
            "BILLED",
            "SAMPLE_COLLECTED",
            "SAMPLE_COLLECTED",
            "IN_PROCESS",
            "IN_PROCESS",
            "RESULT_ENTERED",
            "RESULT_ENTERED",
            "VERIFIED",
            "VERIFIED",
            "COMPLETED");
    if (!Objects.equals(next.get(old), r.status()))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Expected next status: " + next.get(old));
    if ("BILLED".equals(r.status())
        && item.get("invoice_id") != null
        && db.countInvoicesForLabTransition(item.get("invoice_id")) == 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Pay the linked laboratory invoice first");
    if ("SAMPLE_COLLECTED".equals(r.status()))
      db.updateLabOrderItemsForLabTransition("SAMPLE-" + UUID.randomUUID(), id);
    if ("IN_PROCESS".equals(r.status())
        && "OUTSOURCE".equals(item.get("processing_type"))
        && db.countSampleDispatchesForLabTransition(id) == 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Confirm outsourced sample receipt first");
    if ("RESULT_ENTERED".equals(r.status())
        && (r.resultValue() == null || r.resultValue().isBlank())) throw bad("Result required");
    if ("VERIFIED".equals(r.status()) && (r.verifiedBy() == null || r.verifiedBy().isBlank()))
      throw bad("Verifier required");
    db.updateLabOrderItemsForLabTransition2(
        r.status(),
        r.resultValue(),
        r.resultUnit(),
        r.referenceRange(),
        r.status(),
        p.getName(),
        r.status(),
        id);
    db.updateLabOrdersForLabTransition(
        item.get("lab_order_id"), item.get("lab_order_id"), item.get("lab_order_id"));
    audit(p, "LAB_ITEM_" + r.status(), "LAB_ORDER_ITEM", id);
    if ("VERIFIED".equals(r.status()))
      documents.issue(new ClinicalDocumentIssueRequest("LAB_REPORT", id), p);
    return db.findLabOrderItemsForLabTransition2(id);
  }

  @Transactional
  public Map<String, Object> invoice(IntegratedOperationsInvoiceRequest r, Principal p) {
    long pid = required(r.patientId(), "patientId");
    if (!exists("patients", pid)) throw bad("Patient not found");
    if (r.encounterId() != null) {
      Map<String, Object> e = db.findEncountersForInvoice(r.encounterId());
      if (((Number) e.get("patient_id")).longValue() != pid)
        throw bad("Encounter/patient mismatch");
    }
    if (r.items() == null || r.items().isEmpty()) throw bad("Invoice needs items");
    BigDecimal total = BigDecimal.ZERO;
    for (IntegratedOperationsInvoiceLineRequest l : r.items()) {
      positive(l.qty(), "qty");
      positive(l.unitPrice(), "unitPrice");
      if (l.description() == null || l.description().isBlank()) throw bad("description required");
      total = total.add(l.qty().multiply(l.unitPrice()));
    }
    db.insertInvoicesForInvoice(
        id("INV"),
        pid,
        r.encounterId(),
        r.departmentId() != null ? r.departmentId() : ledger.department(null, r.encounterId()),
        total);
    long iid = newId();
    db.updateInvoicesForInvoice(iid);
    for (IntegratedOperationsInvoiceLineRequest l : r.items())
      db.insertInvoiceItemsForInvoice2(
          iid,
          l.itemType(),
          l.referenceId(),
          l.description(),
          l.qty(),
          l.unitPrice(),
          l.qty().multiply(l.unitPrice()));
    audit(p, "INVOICE_CREATED", "INVOICE", iid);
    return db.findInvoicesForInvoice(iid);
  }

  @Transactional
  public Map<String, Object> payment(long id, IntegratedOperationsPaymentRequest r, Principal p) {
    BigDecimal amount = positive(r.amount(), "amount");
    Map<String, Object> invoice = db.findInvoicesForPayment(id);
    BigDecimal total = (BigDecimal) invoice.get("total"), paid = (BigDecimal) invoice.get("paid");
    if (paid.add(amount).add(ledger.insurancePending(id)).compareTo(total) > 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment exceeds balance");
    if (r.method() == null || !Set.of("CASH", "UPI", "CARD", "BANK").contains(r.method()))
      throw bad("Invalid method");
    if (!"CASH".equals(r.method())
        && (!Boolean.TRUE.equals(r.verified())
            || r.referenceNo() == null
            || r.referenceNo().isBlank())) throw bad("Verified payment reference required");
    db.insertPaymentsForPayment(id, amount, r.method(), r.referenceNo());
    BigDecimal updated = paid.add(amount);
    db.updateInvoicesForPayment(
        updated, updated.compareTo(total) == 0 ? "PAID" : "PARTIALLY_PAID", id);
    ledger.status(id);
    audit(p, "PAYMENT_RECORDED", "INVOICE", id);
    return db.findInvoicesForPayment2(id);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @Transactional
  public Map<String, Object> dispense(long id, IntegratedOperationsStockRequest r, Principal p) {
    BigDecimal qty = positive(r.qty(), "qty");
    Map<String, Object> stock = db.findInventoryItemsForDispense(id);
    Object expiryValue = stock.get("expires_on");
    java.time.LocalDate expiry =
        expiryValue instanceof java.time.LocalDate d
            ? d
            : expiryValue instanceof java.sql.Date d ? d.toLocalDate() : null;
    if (expiry != null && expiry.isBefore(java.time.LocalDate.now()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock batch expired");
    BigDecimal available = (BigDecimal) stock.get("stock_qty");
    if (available.compareTo(qty) < 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
    db.updateInventoryItemsForDispense(qty, id);
    db.insertStockMovementsForDispense(
        id, qty.negate(), r.reason() == null ? "DISPENSE" : r.reason(), p.getName());
    audit(p, "STOCK_DISPENSED", "INVENTORY_ITEM", id);
    return db.findInventoryItemsForDispense2(id);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @Transactional
  public Map<String, Object> receive(long id, IntegratedOperationsStockRequest r, Principal p) {
    BigDecimal qty = positive(r.qty(), "qty");
    db.findInventoryItemsForReceive(id);
    db.updateInventoryItemsForReceive(qty, id);
    db.insertStockMovementsForReceive(
        id, qty, r.reason() == null ? "RECEIPT" : r.reason(), p.getName());
    audit(p, "STOCK_RECEIVED", "INVENTORY_ITEM", id);
    return db.findInventoryItemsForReceive2(id);
  }

  @Transactional
  public Map<String, Object> admit(IntegratedOperationsAdmitRequest r, Principal p) {
    long pid = required(r.patientId(), "patientId");
    if (!exists("patients", pid)) throw bad("Patient not found");
    db.findPatientsForAdmit(pid);
    if (db.countAdmissionsForAdmit(pid) > 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Patient already has an active admission; use ward/ICU transfer");
    if (r.ward() == null || r.ward().isBlank() || r.bedNo() == null || r.bedNo().isBlank())
      throw bad("Ward and bed required");
    // Serialize admissions for a bed through its locked bed row.
    List<Map<String, Object>> beds = db.findIpdBedsForAdmit(r.ward(), r.bedNo());
    if (beds.isEmpty()) throw bad("Register bed in ipd_beds first");
    if (!"AVAILABLE".equals(beds.get(0).get("status"))
        && !("RESERVED".equals(beds.get(0).get("status"))
            && beds.get(0).get("reserved_patient_id") instanceof Number reserve
            && reserve.longValue() == pid))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Bed unavailable");
    long bedId = ((Number) beds.get(0).get("id")).longValue();
    Integer occupied = db.readIpdBedsForAdmit(bedId);
    if (occupied != null && occupied != 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Bed occupied");
    if (r.encounterId() != null) {
      Map<String, Object> e = db.findEncountersForAdmit(r.encounterId());
      if (((Number) e.get("patient_id")).longValue() != pid)
        throw bad("Encounter/patient mismatch");
    }
    db.insertAdmissionsForAdmit(id("IPD"), r.encounterId(), pid, r.ward(), r.bedNo());
    long admissionId = newId();
    db.updateIpdBedsForAdmit(bedId);
    bedCharges.open(admissionId, bedId);
    db.insertBedMovementsForAdmit(admissionId, r.ward() + "/" + r.bedNo(), p.getName());
    audit(p, "PATIENT_ADMITTED", "ADMISSION", admissionId);
    return db.findAdmissionsForAdmit(admissionId);
  }

  @Transactional
  public Map<String, Object> discharge(long id, Principal p) {
    Map<String, Object> a = db.findAdmissionsForDischarge(id);
    if (!"ADMITTED".equals(a.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Admission not active");
    bedCharges.close(id);
    db.insertBedMovementsForDischarge(id, a.get("ward") + "/" + a.get("bed_no"), p.getName());
    db.updateAdmissionsForDischarge(id);
    db.updateIpdBedsForDischarge(a.get("ward"), a.get("bed_no"));
    audit(p, "PATIENT_DISCHARGED", "ADMISSION", id);
    return db.findAdmissionsForDischarge2(id);
  }

  public List<Map<String, Object>> beds() {
    return db.findIpdBedsForBeds();
  }
}
