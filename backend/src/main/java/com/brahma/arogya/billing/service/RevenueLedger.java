package com.brahma.arogya.billing.service;

import com.brahma.arogya.billing.repository.RevenueLedgerJdbcRepository;
import java.math.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RevenueLedger {

  private final RevenueLedgerJdbcRepository db;

  public RevenueLedger(RevenueLedgerJdbcRepository db) {
    this.db = db;
  }

  public static BigDecimal money(BigDecimal n, boolean zero) {
    if (n == null || n.scale() > 2 || n.precision() > 12 || n.signum() < (zero ? 0 : 1))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Valid nonnegative money/quantity with at most two decimals required");
    return n;
  }

  @Transactional
  public long charge(
      long patient,
      Long encounter,
      String key,
      String type,
      long reference,
      String description,
      BigDecimal qty,
      BigDecimal price) {
    money(qty, false);
    money(price, true);
    db.findPatientsForCharge(patient);
    var existing = db.findInvoiceItemsForCharge(key);
    if (!existing.isEmpty()) return existing.get(0);
    Long department = department(type, encounter);
    var invoices = db.findInvoicesForCharge(patient, encounter, department);
    long id;
    if (invoices.isEmpty()) {
      db.insertInvoicesForCharge("INV-" + UUID.randomUUID(), patient, encounter, department);
      id = db.readGeneratedIdForCharge();
    } else id = invoices.get(0);
    BigDecimal amount = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
    db.insertInvoiceItemsForCharge2(id, type, reference, description, qty, price, amount, key);
    db.updateInvoicesForCharge(amount, amount, id);
    status(id);
    return id;
  }

  public Long department(String type, Long encounter) {
    String code =
        switch (Objects.toString(type, "")) {
          case "LAB" -> "LAB";
          case "PHARMACY" -> "PHARM";
          case "BED_STAY", "CONSUMABLE" -> "IPD";
          case "REGISTRATION" -> "REG";
          case "RADIOLOGY" -> "RAD";
          default -> null;
        };
    if (code != null) {
      var ids = db.findDepartmentsForDepartment(code);
      if (!ids.isEmpty()) return ids.get(0);
    }
    return encounter == null ? null : db.readEncountersForDepartment(encounter);
  }

  public BigDecimal insurancePending(long invoice) {
    return db.readInsuranceInvoiceAllocationsForInsurancePending(invoice);
  }

  public void completeVisit(long encounter) {
    if (db.countInvoicesForCompleteVisit(encounter) == 0) {
      db.updateEncountersForCompleteVisit(encounter);
      db.updateAppointmentsForCompleteVisit(encounter);
    }
  }

  public void status(long id) {
    db.updateInvoicesForStatus(id);
    Long encounter = db.readInvoicesForStatus(id);
    if (encounter != null) completeVisit(encounter);
  }
}
