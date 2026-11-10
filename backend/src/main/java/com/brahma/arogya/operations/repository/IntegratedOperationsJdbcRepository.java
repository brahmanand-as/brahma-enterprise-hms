package com.brahma.arogya.operations.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class IntegratedOperationsJdbcRepository {

  private final JdbcTemplate jdbc;

  public IntegratedOperationsJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int insertAuditLogsForAudit(String name, String action, String type, String value4) {
    return jdbc.update(
        "INSERT INTO audit_logs(username,action,entity_type,entity_id) VALUES(?,?,?,?)",
        name,
        action,
        type,
        value4);
  }

  public Integer countGeneratedIdForExists(String table, long id) {
    return jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE id=?", Integer.class, id);
  }

  public Long readGeneratedIdForNewId() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public List<Map<String, Object>> findLabOrderItemsForLabItems() {
    return jdbc.queryForList(
        "SELECT i.*,o.patient_id FROM lab_order_items i JOIN lab_orders o ON o.id=i.lab_order_id"
            + " ORDER BY i.id DESC");
  }

  public Map<String, Object> findEncountersForLabOrder(long eid) {
    return jdbc.queryForMap("SELECT patient_id,status FROM encounters WHERE id=?", eid);
  }

  public int insertLabOrdersForLabOrder(String no, long eid, Object patientId, String name) {
    return jdbc.update(
        "INSERT INTO lab_orders(order_no,encounter_id,patient_id,status,ordered_by,ordered_at)"
            + " VALUES(?,?,?,'ORDERED',?,NOW())",
        no,
        eid,
        patientId,
        name);
  }

  public Map<String, Object> findLabTestCatalogForLabOrder(String testCode) {
    return jdbc.queryForMap(
        "SELECT name,default_processing_type,price,result_unit,reference_range FROM"
            + " lab_test_catalog WHERE code=? AND active=1",
        testCode);
  }

  public int insertLabOrderItemsForLabOrder(
      long oid, String testCode, Object name, String processing, Long externalLabId) {
    return jdbc.update(
        "INSERT INTO"
            + " lab_order_items(lab_order_id,test_code,test_name,processing_type,external_lab_id,status)"
            + " VALUES(?,?,?,?,?,'ORDERED')",
        oid,
        testCode,
        name,
        processing,
        externalLabId);
  }

  public int updateLabOrderItemsForLabOrder(
      long invoice, Object resultUnit, Object referenceRange, long itemId) {
    return jdbc.update(
        "UPDATE lab_order_items SET invoice_id=?,result_unit=?,reference_range=? WHERE id=?",
        invoice,
        resultUnit,
        referenceRange,
        itemId);
  }

  public Map<String, Object> findLabOrdersForLabOrder(long oid) {
    return jdbc.queryForMap("SELECT * FROM lab_orders WHERE id=?", oid);
  }

  public Map<String, Object> findLabOrderItemsForLabTransition(long id) {
    return jdbc.queryForMap("SELECT * FROM lab_order_items WHERE id=? FOR UPDATE", id);
  }

  public Integer countInvoicesForLabTransition(Object invoiceId) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM invoices WHERE id=? AND status='PAID'", Integer.class, invoiceId);
  }

  public int updateLabOrderItemsForLabTransition(String value1, long id) {
    return jdbc.update("UPDATE lab_order_items SET sample_barcode=? WHERE id=?", value1, id);
  }

  public Integer countSampleDispatchesForLabTransition(long id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM sample_dispatches WHERE lab_order_item_id=? AND"
            + " status='RECEIVED'",
        Integer.class,
        id);
  }

  public int updateLabOrderItemsForLabTransition2(
      String status,
      String resultValue,
      String resultUnit,
      String referenceRange,
      String status2,
      String name,
      String status3,
      long id) {
    return jdbc.update(
        "UPDATE lab_order_items SET"
            + " status=?,result_value=COALESCE(?,result_value),result_unit=COALESCE(?,result_unit),reference_range=COALESCE(?,reference_range),verified_by=CASE"
            + " WHEN ?='VERIFIED' THEN ? ELSE verified_by END,verified_at=CASE WHEN ?='VERIFIED'"
            + " THEN NOW() ELSE verified_at END WHERE id=?",
        status,
        resultValue,
        resultUnit,
        referenceRange,
        status2,
        name,
        status3,
        id);
  }

  public int updateLabOrdersForLabTransition(
      Object labOrderId, Object labOrderId2, Object labOrderId3) {
    return jdbc.update(
        "UPDATE lab_orders SET status=CASE WHEN NOT EXISTS(SELECT 1 FROM lab_order_items WHERE"
            + " lab_order_id=? AND status<>'COMPLETED') THEN 'COMPLETED' WHEN NOT EXISTS(SELECT 1"
            + " FROM lab_order_items WHERE lab_order_id=? AND status NOT IN"
            + " ('VERIFIED','COMPLETED')) THEN 'VERIFIED' ELSE 'IN_PROGRESS' END WHERE id=?",
        labOrderId,
        labOrderId2,
        labOrderId3);
  }

  public Map<String, Object> findLabOrderItemsForLabTransition2(long id) {
    return jdbc.queryForMap("SELECT * FROM lab_order_items WHERE id=?", id);
  }

  public Map<String, Object> findEncountersForInvoice(Long encounterId) {
    return jdbc.queryForMap("SELECT patient_id FROM encounters WHERE id=?", encounterId);
  }

  public int insertInvoicesForInvoice(
      String value1, long pid, Long encounterId, Object value4, BigDecimal total) {
    return jdbc.update(
        "INSERT INTO"
            + " invoices(invoice_no,patient_id,encounter_id,department_id,total,paid,status,created_at)"
            + " VALUES(?,?,?,?,?,0,'UNPAID',NOW())",
        value1,
        pid,
        encounterId,
        value4,
        total);
  }

  public int updateInvoicesForInvoice(long iid) {
    return jdbc.update("UPDATE invoices SET gross_total=total WHERE id=?", iid);
  }

  public int insertInvoiceItemsForInvoice2(
      long iid,
      String itemType,
      Long referenceId,
      String description,
      BigDecimal qty,
      BigDecimal unitPrice,
      Object value7) {
    return jdbc.update(
        "INSERT INTO"
            + " invoice_items(invoice_id,item_type,reference_id,description,qty,unit_price,amount)"
            + " VALUES(?,?,?,?,?,?,?)",
        iid,
        itemType,
        referenceId,
        description,
        qty,
        unitPrice,
        value7);
  }

  public Map<String, Object> findInvoicesForInvoice(long iid) {
    return jdbc.queryForMap("SELECT * FROM invoices WHERE id=?", iid);
  }

  public Map<String, Object> findInvoicesForPayment(long id) {
    return jdbc.queryForMap("SELECT * FROM invoices WHERE id=? FOR UPDATE", id);
  }

  public int insertPaymentsForPayment(
      long id, BigDecimal amount, String method, String referenceNo) {
    return jdbc.update(
        "INSERT INTO payments(invoice_id,amount,method,reference_no,paid_at) VALUES(?,?,?,?,NOW())",
        id,
        amount,
        method,
        referenceNo);
  }

  public int updateInvoicesForPayment(BigDecimal updated, String value2, long id) {
    return jdbc.update("UPDATE invoices SET paid=?,status=? WHERE id=?", updated, value2, id);
  }

  public Map<String, Object> findInvoicesForPayment2(long id) {
    return jdbc.queryForMap("SELECT * FROM invoices WHERE id=?", id);
  }

  public Map<String, Object> findInventoryItemsForDispense(long id) {
    return jdbc.queryForMap("SELECT * FROM inventory_items WHERE id=? FOR UPDATE", id);
  }

  public int updateInventoryItemsForDispense(BigDecimal qty, long id) {
    return jdbc.update("UPDATE inventory_items SET stock_qty=stock_qty-? WHERE id=?", qty, id);
  }

  public int insertStockMovementsForDispense(
      long id, BigDecimal negate, Object value3, String name) {
    return jdbc.update(
        "INSERT INTO"
            + " stock_movements(inventory_item_id,quantity_change,reason,performed_by,created_at)"
            + " VALUES(?,?,?,?,NOW())",
        id,
        negate,
        value3,
        name);
  }

  public Map<String, Object> findInventoryItemsForDispense2(long id) {
    return jdbc.queryForMap("SELECT * FROM inventory_items WHERE id=?", id);
  }

  public Map<String, Object> findInventoryItemsForReceive(long id) {
    return jdbc.queryForMap("SELECT id FROM inventory_items WHERE id=? FOR UPDATE", id);
  }

  public int updateInventoryItemsForReceive(BigDecimal qty, long id) {
    return jdbc.update("UPDATE inventory_items SET stock_qty=stock_qty+? WHERE id=?", qty, id);
  }

  public int insertStockMovementsForReceive(long id, BigDecimal qty, Object value3, String name) {
    return jdbc.update(
        "INSERT INTO"
            + " stock_movements(inventory_item_id,quantity_change,reason,performed_by,created_at)"
            + " VALUES(?,?,?,?,NOW())",
        id,
        qty,
        value3,
        name);
  }

  public Map<String, Object> findInventoryItemsForReceive2(long id) {
    return jdbc.queryForMap("SELECT * FROM inventory_items WHERE id=?", id);
  }

  public Map<String, Object> findPatientsForAdmit(long pid) {
    return jdbc.queryForMap("SELECT id FROM patients WHERE id=? FOR UPDATE", pid);
  }

  public Integer countAdmissionsForAdmit(long pid) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM admissions WHERE patient_id=? AND status='ADMITTED'",
        Integer.class,
        pid);
  }

  public List<Map<String, Object>> findIpdBedsForAdmit(String ward, String bedNo) {
    return jdbc.queryForList(
        "SELECT id,status,reserved_patient_id FROM ipd_beds WHERE ward=? AND bed_no=? FOR"
            + " UPDATE",
        ward,
        bedNo);
  }

  public Integer readIpdBedsForAdmit(long bedId) {
    return jdbc.queryForObject("SELECT occupied FROM ipd_beds WHERE id=?", Integer.class, bedId);
  }

  public Map<String, Object> findEncountersForAdmit(Long encounterId) {
    return jdbc.queryForMap("SELECT patient_id FROM encounters WHERE id=?", encounterId);
  }

  public int insertAdmissionsForAdmit(
      String value1, Long encounterId, long pid, String ward, String bedNo) {
    return jdbc.update(
        "INSERT INTO"
            + " admissions(admission_no,encounter_id,patient_id,ward,bed_no,status,admitted_at)"
            + " VALUES(?,?,?,?,?,'ADMITTED',NOW())",
        value1,
        encounterId,
        pid,
        ward,
        bedNo);
  }

  public int updateIpdBedsForAdmit(long bedId) {
    return jdbc.update(
        "UPDATE ipd_beds SET occupied=1,status='OCCUPIED',reserved_patient_id=NULL WHERE id=?",
        bedId);
  }

  public int insertBedMovementsForAdmit(long admissionId, Object value2, String name) {
    return jdbc.update(
        "INSERT INTO bed_movements(admission_id,to_bed,action,performed_by) VALUES(?,?,'ADMIT',?)",
        admissionId,
        value2,
        name);
  }

  public Map<String, Object> findAdmissionsForAdmit(long admissionId) {
    return jdbc.queryForMap("SELECT * FROM admissions WHERE id=?", admissionId);
  }

  public Map<String, Object> findAdmissionsForDischarge(long id) {
    return jdbc.queryForMap("SELECT * FROM admissions WHERE id=? FOR UPDATE", id);
  }

  public int insertBedMovementsForDischarge(long id, String value2, String name) {
    return jdbc.update(
        "INSERT INTO bed_movements(admission_id,from_bed,action,performed_by) VALUES(?,"
            + " ?,'DISCHARGE',?)",
        id,
        value2,
        name);
  }

  public int updateAdmissionsForDischarge(long id) {
    return jdbc.update(
        "UPDATE admissions SET status='DISCHARGED',discharged_at=NOW() WHERE id=?", id);
  }

  public int updateIpdBedsForDischarge(Object ward, Object bedNo) {
    return jdbc.update(
        "UPDATE ipd_beds SET occupied=0,status='CLEANING' WHERE ward=? AND bed_no=?", ward, bedNo);
  }

  public Map<String, Object> findAdmissionsForDischarge2(long id) {
    return jdbc.queryForMap("SELECT * FROM admissions WHERE id=?", id);
  }

  public List<Map<String, Object>> findIpdBedsForBeds() {
    return jdbc.queryForList("SELECT * FROM ipd_beds ORDER BY ward,bed_no");
  }
}
