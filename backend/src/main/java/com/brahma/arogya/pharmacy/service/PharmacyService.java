package com.brahma.arogya.pharmacy.service;

import com.brahma.arogya.billing.service.RevenueLedger;
import com.brahma.arogya.pharmacy.dto.PharmacyConsumableReturnRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyDispenseRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyPurchaseRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyReceiptRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyStockActionRequest;
import com.brahma.arogya.pharmacy.dto.PharmacySupplierRequest;
import com.brahma.arogya.pharmacy.repository.PharmacyJdbcRepository;
import com.brahma.arogya.security.service.CareScope;
import java.math.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class PharmacyService {

  private final CareScope care;

  private final PharmacyJdbcRepository db;

  private final RevenueLedger ledger;

  public PharmacyService(PharmacyJdbcRepository db, RevenueLedger ledger, CareScope care) {
    this.db = db;
    this.ledger = ledger;
    this.care = care;
  }

  public List<Map<String, Object>> batches() {
    return db.findInventoryBatchesForBatches();
  }

  public List<Map<String, Object>> suppliers() {
    return db.findSuppliersForSuppliers();
  }

  public Map<String, String> supplier(PharmacySupplierRequest s) {
    if (s.name() == null || s.name().isBlank()) throw bad("Supplier name required");
    db.insertSuppliersForSupplier(s.name(), s.contact());
    return Map.of("status", "CREATED");
  }

  public List<Map<String, Object>> purchases() {
    var list = db.findPurchaseOrdersForPurchases();
    for (var p : list) p.put("lines", db.findPurchaseOrderLinesForPurchases(p.get("id")));
    return list;
  }

  @Transactional
  public Map<String, Object> purchase(PharmacyPurchaseRequest f, Principal p) {
    if (f.supplierId() == null || f.lines() == null || f.lines().isEmpty())
      throw bad("Supplier and purchase lines required");
    db.insertPurchaseOrdersForPurchase(f.supplierId(), p.getName());
    long id = id();
    for (var l : f.lines()) {
      RevenueLedger.money(l.quantity(), false);
      RevenueLedger.money(l.unitCost(), true);
      db.insertPurchaseOrderLinesForPurchase(id, l.inventoryItemId(), l.quantity(), l.unitCost());
    }
    return Map.of("id", id, "status", "ORDERED");
  }

  @Transactional
  public Map<String, Object> receipt(PharmacyReceiptRequest f, Principal p) {
    RevenueLedger.money(f.quantity(), false);
    RevenueLedger.money(f.salePrice(), true);
    if (f.purchaseLineId() == null
        || f.batchNo() == null
        || f.batchNo().isBlank()
        || f.expiresOn() == null
        || f.expiresOn().isBefore(LocalDate.now()))
      throw bad("Purchase line, valid batch and expiry required");
    var line = db.findPurchaseOrderLinesForReceipt(f.purchaseLineId());
    if (((BigDecimal) line.get("received"))
            .add(f.quantity())
            .compareTo((BigDecimal) line.get("quantity"))
        > 0) throw conflict("Receipt exceeds purchase quantity");
    db.insertInventoryBatchesForReceipt(
        line.get("inventory_item_id"),
        f.batchNo(),
        f.expiresOn(),
        f.quantity(),
        line.get("unit_cost"),
        f.salePrice());
    long batch = id();
    move(batch, f.quantity(), "GRN", f.purchaseLineId(), null, null, p, "Goods received");
    db.updatePurchaseOrderLinesForReceipt(f.quantity(), f.purchaseLineId());
    db.updateInventoryItemsForReceipt(f.quantity(), line.get("inventory_item_id"));
    db.updatePurchaseOrdersForReceipt(line.get("purchase_order_id"), line.get("purchase_order_id"));
    return Map.of("batchId", batch, "status", "RECEIVED");
  }

  public List<Map<String, Object>> dispenses() {
    return db.findPharmacyDispensesForDispenses();
  }

  @Transactional
  public Map<String, Object> dispense(PharmacyDispenseRequest f, Principal p) {
    RevenueLedger.money(f.quantity(), false);
    if (f.patientId() == null
        || f.inventoryItemId() == null
        || (f.prescriptionId() == null && f.medicationOrderId() == null))
      throw bad(
          "Patient, stock item and finalized prescription or verified medication order required");
    Long encounter = null;
    if (f.prescriptionId() != null) {
      var rx = db.findPrescriptionsForDispense(f.prescriptionId());
      if (!"FINAL".equals(rx.get("status"))
          || ((Number) rx.get("patient_id")).longValue() != f.patientId())
        throw conflict("Final prescription and patient mismatch");
      String stockName = db.readInventoryItemsForDispense(f.inventoryItemId());
      if (db.countPrescriptionItemsForDispense(f.prescriptionId(), stockName) == 0)
        throw bad("Selected medicine is not on this prescription");
      encounter = ((Number) rx.get("encounter_id")).longValue();
    }
    if (f.medicationOrderId() != null) {
      var o = db.findMedicationOrdersForDispense(f.medicationOrderId());
      if (!"VERIFIED".equals(o.get("status"))
          || ((Number) o.get("patient_id")).longValue() != f.patientId()
          || ((Number) o.get("inventory_item_id")).longValue() != f.inventoryItemId())
        throw conflict("Medication must be verified and match the patient/stock");
      BigDecimal prior = db.readPharmacyDispensesForDispense(f.medicationOrderId());
      BigDecimal ordered =
          ((BigDecimal) o.get("quantity_per_dose"))
              .multiply(BigDecimal.valueOf(((Number) o.get("dose_count")).longValue()));
      if (prior.add(f.quantity()).compareTo(ordered) > 0)
        throw conflict("Dispense exceeds medication order quantity");
    }
    db.findInventoryItemsForDispense(f.inventoryItemId());
    var batches = db.findInventoryBatchesForDispense(f.inventoryItemId());
    BigDecimal available =
        batches.stream()
            .map(b -> (BigDecimal) b.get("quantity"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (available.compareTo(f.quantity()) < 0) throw conflict("Insufficient unexpired stock");
    db.insertPharmacyDispensesForDispense(
        f.patientId(),
        f.prescriptionId(),
        f.medicationOrderId(),
        f.inventoryItemId(),
        f.quantity(),
        p.getName());
    long id = id();
    BigDecimal remaining = f.quantity(), total = BigDecimal.ZERO;
    for (var b : batches) {
      BigDecimal take = remaining.min((BigDecimal) b.get("quantity"));
      if (take.signum() == 0) break;
      BigDecimal price = (BigDecimal) b.get("sale_price");
      db.updateInventoryBatchesForDispense(take, b.get("id"));
      db.insertDispenseAllocationsForDispense(id, b.get("id"), take, price);
      move(
          ((Number) b.get("id")).longValue(),
          take.negate(),
          "DISPENSE",
          id,
          f.patientId(),
          null,
          p,
          "Patient dispense");
      total = total.add(take.multiply(price));
      remaining = remaining.subtract(take);
    }
    db.updateInventoryItemsForDispense(f.quantity(), f.inventoryItemId());
    db.updatePharmacyDispensesForDispense(total, id);
    ledger.charge(
        f.patientId(),
        encounter,
        "DISPENSE-" + id,
        "PHARMACY",
        id,
        "Pharmacy dispense",
        BigDecimal.ONE,
        total);
    return Map.of("id", id, "charge", total, "status", "DISPENSED");
  }

  @Transactional
  public Map<String, Object> returns(long id, PharmacyStockActionRequest f, Principal p) {
    RevenueLedger.money(f.quantity(), false);
    if (f.reason() == null || f.reason().isBlank()) throw bad("Return reason required");
    var d = db.findPharmacyDispensesForReturns(id);
    if (d.get("medication_order_id") != null) {
      var o = db.findMedicationOrdersForReturns(d.get("medication_order_id"));
      BigDecimal supplied = db.readPharmacyDispensesForReturns(o.get("id"));
      long given = db.countMedicationAdministrationsForReturns(o.get("id"));
      if (supplied
              .subtract(
                  ((BigDecimal) o.get("quantity_per_dose")).multiply(BigDecimal.valueOf(given)))
              .compareTo(f.quantity())
          < 0) throw conflict("Administered medication cannot be returned");
    }
    if (((BigDecimal) d.get("returned")).add(f.quantity()).compareTo((BigDecimal) d.get("quantity"))
        > 0) throw conflict("Return exceeds dispensed quantity");
    var allocations = db.findDispenseAllocationsForReturns(id);
    BigDecimal left = f.quantity(), credit = BigDecimal.ZERO;
    for (var a : allocations) {
      BigDecimal take =
          left.min(((BigDecimal) a.get("quantity")).subtract((BigDecimal) a.get("returned")));
      if (take.signum() <= 0) continue;
      db.findInventoryBatchesForReturns(a.get("batch_id"));
      db.updateDispenseAllocationsForReturns(take, a.get("id"));
      db.updateInventoryBatchesForReturns(take, a.get("batch_id"));
      move(
          ((Number) a.get("batch_id")).longValue(),
          take,
          "RETURN",
          id,
          ((Number) d.get("patient_id")).longValue(),
          null,
          p,
          f.reason());
      credit = credit.add(take.multiply((BigDecimal) a.get("unit_price")));
      left = left.subtract(take);
      if (left.signum() == 0) break;
    }
    db.updatePharmacyDispensesForReturns(f.quantity(), id);
    db.updateInventoryItemsForReturns(f.quantity(), d.get("inventory_item_id"));
    if (credit.signum() > 0) {
      Long invoice = db.readInvoiceItemsForReturns("DISPENSE-" + id);
      db.insertReturnCreditRequestsForReturns(id, invoice, credit, f.reason(), p.getName());
    }
    return Map.of("status", "RETURNED", "creditToReconcile", credit);
  }

  @Transactional
  public Map<String, String> action(
      long id, String action, PharmacyStockActionRequest f, Principal p) {
    RevenueLedger.money(f.quantity(), false);
    if (!Set.of("WASTE", "DAMAGE", "WARD_RECEIPT").contains(action)
        || f.reason() == null
        || f.reason().isBlank()) throw bad("Stock action and reason required");
    var b = db.findInventoryBatchesForAction(id);
    if (((BigDecimal) b.get("quantity")).compareTo(f.quantity()) < 0)
      throw conflict("Insufficient batch stock");
    if (action.equals("WARD_RECEIPT")
        && (f.ward() == null || db.countIpdBedsForAction(f.ward()) == 0))
      throw bad("Valid ward required");
    db.updateInventoryBatchesForAction(f.quantity(), id);
    db.updateInventoryItemsForAction(f.quantity(), b.get("inventory_item_id"));
    if (action.equals("WARD_RECEIPT")) db.insertWardStockForAction(id, f.ward(), f.quantity());
    move(id, f.quantity().negate(), action, null, null, f.ward(), p, f.reason());
    return Map.of("status", action);
  }

  public List<Map<String, Object>> wardStock() {
    return db.findWardStockForWardStock();
  }

  @Transactional
  public Map<String, String> consume(long id, PharmacyStockActionRequest f, Principal p) {
    RevenueLedger.money(f.quantity(), false);
    if (f.admissionId() == null || f.patientId() == null || f.ward() == null)
      throw bad("Active admission, patient and ward required");
    var a = db.findAdmissionsForConsume(f.admissionId());
    if (((Number) a.get("patient_id")).longValue() != f.patientId()
        || !a.get("ward").equals(f.ward())) throw conflict("Patient/ward/admission mismatch");
    var w = db.findWardStockForConsume(id, f.ward());
    var b = db.findInventoryBatchesForConsume(id);
    if (date(b.get("expires_on")).isBefore(LocalDate.now())) throw conflict("Batch expired");
    if (((BigDecimal) w.get("quantity")).compareTo(f.quantity()) < 0)
      throw conflict("Insufficient ward stock");
    db.updateWardStockForConsume(f.quantity(), id, f.ward());
    move(
        id,
        f.quantity().negate(),
        "PATIENT_CONSUMABLE",
        f.admissionId(),
        f.patientId(),
        f.ward(),
        p,
        f.reason());
    long movement = this.id();
    ledger.charge(
        f.patientId(),
        a.get("encounter_id") == null ? null : ((Number) a.get("encounter_id")).longValue(),
        "CONSUMABLE-" + movement,
        "CONSUMABLE",
        movement,
        "Ward consumable",
        f.quantity(),
        (BigDecimal) b.get("sale_price"));
    return Map.of("status", "CONSUMED");
  }

  @Transactional
  public Map<String, String> wardAction(
      long id, String action, PharmacyStockActionRequest f, Principal p) {
    RevenueLedger.money(f.quantity(), false);
    if (!Set.of("RETURN", "WASTE", "DAMAGE").contains(action)
        || f.ward() == null
        || f.reason() == null
        || f.reason().isBlank()) throw bad("Ward, action and reason required");
    var w = db.findWardStockForWardAction(id, f.ward());
    if (((BigDecimal) w.get("quantity")).compareTo(f.quantity()) < 0)
      throw conflict("Insufficient ward stock");
    var b = db.findInventoryBatchesForWardAction(id);
    db.updateWardStockForWardAction(f.quantity(), id, f.ward());
    if (action.equals("RETURN")) {
      db.updateInventoryBatchesForWardAction(f.quantity(), id);
      db.updateInventoryItemsForWardAction(f.quantity(), b.get("inventory_item_id"));
    }
    move(id, f.quantity().negate(), "WARD_" + action, null, null, f.ward(), p, f.reason());
    return Map.of("status", action);
  }

  @Transactional
  public Map<String, String> consumableReturn(PharmacyConsumableReturnRequest f, Principal p) {
    RevenueLedger.money(f.quantity(), false);
    if (f.movementId() == null || f.reason() == null || f.reason().isBlank())
      throw bad("Original consumption and return reason required");
    var m = db.findBatchMovementsForConsumableReturn(f.movementId());
    care.require(((Number) m.get("patient_id")).longValue());
    BigDecimal prior = db.readConsumableReturnsForConsumableReturn(f.movementId());
    if (prior.add(f.quantity()).compareTo(((BigDecimal) m.get("quantity")).abs()) > 0)
      throw conflict("Return exceeds original patient consumption");
    db.findWardStockForConsumableReturn(m.get("batch_id"), m.get("ward"));
    db.updateWardStockForConsumableReturn(f.quantity(), m.get("batch_id"), m.get("ward"));
    db.insertConsumableReturnsForConsumableReturn(
        f.movementId(), f.quantity(), f.reason(), p.getName());
    var line = db.findInvoiceItemsForConsumableReturn("CONSUMABLE-" + f.movementId());
    BigDecimal credit =
        f.quantity()
            .multiply((BigDecimal) line.get("unit_price"))
            .setScale(2, RoundingMode.HALF_UP);
    if (credit.signum() > 0)
      db.insertReturnCreditRequestsForConsumableReturn(
          line.get("invoice_id"), credit, f.reason(), p.getName());
    move(
        ((Number) m.get("batch_id")).longValue(),
        f.quantity(),
        "CONSUMABLE_RETURN",
        f.movementId(),
        ((Number) m.get("patient_id")).longValue(),
        String.valueOf(m.get("ward")),
        p,
        f.reason());
    return Map.of("status", "RETURNED");
  }

  public List<Map<String, Object>> consumables() {
    return db.findBatchMovementsForConsumables();
  }

  public List<Map<String, Object>> movements() {
    return db.findBatchMovementsForMovements();
  }

  private void move(
      long batch,
      BigDecimal quantity,
      String reason,
      Long reference,
      Long patient,
      String ward,
      Principal p,
      String notes) {
    db.insertBatchMovementsForMove(
        batch, quantity, reason, reference, patient, ward, p.getName(), notes);
  }

  private LocalDate date(Object o) {
    return o instanceof LocalDate d ? d : ((java.sql.Date) o).toLocalDate();
  }

  private long id() {
    return db.readGeneratedIdForId();
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  private ResponseStatusException conflict(String s) {
    return new ResponseStatusException(HttpStatus.CONFLICT, s);
  }
}
