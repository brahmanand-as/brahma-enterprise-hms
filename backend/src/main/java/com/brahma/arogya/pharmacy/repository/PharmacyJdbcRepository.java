package com.brahma.arogya.pharmacy.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class PharmacyJdbcRepository {

  private final JdbcTemplate jdbc;

  public PharmacyJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findInventoryBatchesForBatches() {
    return jdbc.queryForList(
        "SELECT b.*,i.name,i.sku FROM inventory_batches b JOIN inventory_items i ON"
            + " i.id=b.inventory_item_id ORDER BY b.expires_on,b.id");
  }

  public List<Map<String, Object>> findSuppliersForSuppliers() {
    return jdbc.queryForList("SELECT * FROM suppliers ORDER BY name");
  }

  public int insertSuppliersForSupplier(String name, String contact) {
    return jdbc.update("INSERT INTO suppliers(name,contact) VALUES(?,?)", name, contact);
  }

  public List<Map<String, Object>> findPurchaseOrdersForPurchases() {
    return jdbc.queryForList(
        "SELECT p.*,s.name supplier FROM purchase_orders p JOIN suppliers s ON"
            + " s.id=p.supplier_id ORDER BY p.id DESC");
  }

  public List<Map<String, Object>> findPurchaseOrderLinesForPurchases(Object id) {
    return jdbc.queryForList(
        "SELECT l.*,i.name FROM purchase_order_lines l JOIN inventory_items i ON"
            + " i.id=l.inventory_item_id WHERE purchase_order_id=?",
        id);
  }

  public int insertPurchaseOrdersForPurchase(Long supplierId, String name) {
    return jdbc.update(
        "INSERT INTO purchase_orders(supplier_id,created_by) VALUES(?,?)", supplierId, name);
  }

  public int insertPurchaseOrderLinesForPurchase(
      long id, Object inventoryItemId, Object quantity, Object unitCost) {
    return jdbc.update(
        "INSERT INTO purchase_order_lines(purchase_order_id,inventory_item_id,quantity,unit_cost)"
            + " VALUES(?,?,?,?)",
        id,
        inventoryItemId,
        quantity,
        unitCost);
  }

  public Map<String, Object> findPurchaseOrderLinesForReceipt(Long purchaseLineId) {
    return jdbc.queryForMap(
        "SELECT * FROM purchase_order_lines WHERE id=? FOR UPDATE", purchaseLineId);
  }

  public int insertInventoryBatchesForReceipt(
      Object inventoryItemId,
      String batchNo,
      LocalDate expiresOn,
      BigDecimal quantity,
      Object unitCost,
      BigDecimal salePrice) {
    return jdbc.update(
        "INSERT INTO"
            + " inventory_batches(inventory_item_id,batch_no,expires_on,quantity,unit_cost,sale_price)"
            + " VALUES(?,?,?,?,?,?)",
        inventoryItemId,
        batchNo,
        expiresOn,
        quantity,
        unitCost,
        salePrice);
  }

  public int updatePurchaseOrderLinesForReceipt(BigDecimal quantity, Long purchaseLineId) {
    return jdbc.update(
        "UPDATE purchase_order_lines SET received=received+? WHERE id=?", quantity, purchaseLineId);
  }

  public int updateInventoryItemsForReceipt(BigDecimal quantity, Object inventoryItemId) {
    return jdbc.update(
        "UPDATE inventory_items SET stock_qty=stock_qty+? WHERE id=?", quantity, inventoryItemId);
  }

  public int updatePurchaseOrdersForReceipt(Object purchaseOrderId, Object purchaseOrderId2) {
    return jdbc.update(
        "UPDATE purchase_orders SET status=CASE WHEN EXISTS(SELECT 1 FROM purchase_order_lines"
            + " WHERE purchase_order_id=? AND received<quantity) THEN 'PARTIALLY_RECEIVED' ELSE"
            + " 'RECEIVED' END WHERE id=?",
        purchaseOrderId,
        purchaseOrderId2);
  }

  public List<Map<String, Object>> findPharmacyDispensesForDispenses() {
    return jdbc.queryForList(
        "SELECT d.*,i.name,p.uhid,CONCAT(p.first_name,' ',COALESCE(p.last_name,'')) patient_name"
            + " FROM pharmacy_dispenses d JOIN inventory_items i ON i.id=d.inventory_item_id JOIN"
            + " patients p ON p.id=d.patient_id ORDER BY d.id DESC");
  }

  public Map<String, Object> findPrescriptionsForDispense(Long prescriptionId) {
    return jdbc.queryForMap("SELECT * FROM prescriptions WHERE id=? FOR UPDATE", prescriptionId);
  }

  public String readInventoryItemsForDispense(Long inventoryItemId) {
    return jdbc.queryForObject(
        "SELECT name FROM inventory_items WHERE id=?", String.class, inventoryItemId);
  }

  public Integer countPrescriptionItemsForDispense(Long prescriptionId, String stockName) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM prescription_items WHERE prescription_id=? AND medicine_name=?",
        Integer.class,
        prescriptionId,
        stockName);
  }

  public Map<String, Object> findMedicationOrdersForDispense(Long medicationOrderId) {
    return jdbc.queryForMap(
        "SELECT * FROM medication_orders WHERE id=? FOR UPDATE", medicationOrderId);
  }

  public BigDecimal readPharmacyDispensesForDispense(Long medicationOrderId) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(quantity-returned),0) FROM pharmacy_dispenses WHERE"
            + " medication_order_id=?",
        BigDecimal.class,
        medicationOrderId);
  }

  public Map<String, Object> findInventoryItemsForDispense(Long inventoryItemId) {
    return jdbc.queryForMap(
        "SELECT id FROM inventory_items WHERE id=? FOR UPDATE", inventoryItemId);
  }

  public List<Map<String, Object>> findInventoryBatchesForDispense(Long inventoryItemId) {
    return jdbc.queryForList(
        "SELECT * FROM inventory_batches WHERE inventory_item_id=? AND expires_on>=CURDATE()"
            + " AND quantity>0 ORDER BY expires_on,id FOR UPDATE",
        inventoryItemId);
  }

  public int insertPharmacyDispensesForDispense(
      Long patientId,
      Long prescriptionId,
      Long medicationOrderId,
      Long inventoryItemId,
      BigDecimal quantity,
      String name) {
    return jdbc.update(
        "INSERT INTO"
            + " pharmacy_dispenses(patient_id,prescription_id,medication_order_id,inventory_item_id,quantity,charge_amount,performed_by)"
            + " VALUES(?,?,?,?,?,0,?)",
        patientId,
        prescriptionId,
        medicationOrderId,
        inventoryItemId,
        quantity,
        name);
  }

  public int updateInventoryBatchesForDispense(BigDecimal take, Object id) {
    return jdbc.update("UPDATE inventory_batches SET quantity=quantity-? WHERE id=?", take, id);
  }

  public int insertDispenseAllocationsForDispense(
      long id, Object id2, BigDecimal take, BigDecimal price) {
    return jdbc.update(
        "INSERT INTO dispense_allocations(dispense_id,batch_id,quantity,unit_price)"
            + " VALUES(?,?,?,?)",
        id,
        id2,
        take,
        price);
  }

  public int updateInventoryItemsForDispense(BigDecimal quantity, Long inventoryItemId) {
    return jdbc.update(
        "UPDATE inventory_items SET stock_qty=stock_qty-? WHERE id=?", quantity, inventoryItemId);
  }

  public int updatePharmacyDispensesForDispense(BigDecimal total, long id) {
    return jdbc.update("UPDATE pharmacy_dispenses SET charge_amount=? WHERE id=?", total, id);
  }

  public Map<String, Object> findPharmacyDispensesForReturns(long id) {
    return jdbc.queryForMap("SELECT * FROM pharmacy_dispenses WHERE id=? FOR UPDATE", id);
  }

  public Map<String, Object> findMedicationOrdersForReturns(Object medicationOrderId) {
    return jdbc.queryForMap(
        "SELECT * FROM medication_orders WHERE id=? FOR UPDATE", medicationOrderId);
  }

  public BigDecimal readPharmacyDispensesForReturns(Object id) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(quantity-returned),0) FROM pharmacy_dispenses WHERE"
            + " medication_order_id=?",
        BigDecimal.class,
        id);
  }

  public Long countMedicationAdministrationsForReturns(Object id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM medication_administrations WHERE medication_order_id=? AND"
            + " status='GIVEN'",
        Long.class,
        id);
  }

  public List<Map<String, Object>> findDispenseAllocationsForReturns(long id) {
    return jdbc.queryForList(
        "SELECT * FROM dispense_allocations WHERE dispense_id=? ORDER BY id FOR UPDATE", id);
  }

  public Map<String, Object> findInventoryBatchesForReturns(Object batchId) {
    return jdbc.queryForMap("SELECT id FROM inventory_batches WHERE id=? FOR UPDATE", batchId);
  }

  public int updateDispenseAllocationsForReturns(BigDecimal take, Object id) {
    return jdbc.update("UPDATE dispense_allocations SET returned=returned+? WHERE id=?", take, id);
  }

  public int updateInventoryBatchesForReturns(BigDecimal take, Object batchId) {
    return jdbc.update(
        "UPDATE inventory_batches SET quantity=quantity+? WHERE id=?", take, batchId);
  }

  public int updatePharmacyDispensesForReturns(BigDecimal quantity, long id) {
    return jdbc.update(
        "UPDATE pharmacy_dispenses SET returned=returned+? WHERE id=?", quantity, id);
  }

  public int updateInventoryItemsForReturns(BigDecimal quantity, Object inventoryItemId) {
    return jdbc.update(
        "UPDATE inventory_items SET stock_qty=stock_qty+? WHERE id=?", quantity, inventoryItemId);
  }

  public Long readInvoiceItemsForReturns(String value2) {
    return jdbc.queryForObject(
        "SELECT invoice_id FROM invoice_items WHERE charge_key=?", Long.class, value2);
  }

  public int insertReturnCreditRequestsForReturns(
      long id, Long invoice, BigDecimal credit, String reason, String name) {
    return jdbc.update(
        "INSERT INTO return_credit_requests(dispense_id,invoice_id,amount,reason,requested_by)"
            + " VALUES(?,?,?,?,?)",
        id,
        invoice,
        credit,
        reason,
        name);
  }

  public Map<String, Object> findInventoryBatchesForAction(long id) {
    return jdbc.queryForMap("SELECT * FROM inventory_batches WHERE id=? FOR UPDATE", id);
  }

  public Integer countIpdBedsForAction(String ward) {
    return jdbc.queryForObject("SELECT COUNT(*) FROM ipd_beds WHERE ward=?", Integer.class, ward);
  }

  public int updateInventoryBatchesForAction(BigDecimal quantity, long id) {
    return jdbc.update("UPDATE inventory_batches SET quantity=quantity-? WHERE id=?", quantity, id);
  }

  public int updateInventoryItemsForAction(BigDecimal quantity, Object inventoryItemId) {
    return jdbc.update(
        "UPDATE inventory_items SET stock_qty=stock_qty-? WHERE id=?", quantity, inventoryItemId);
  }

  public int insertWardStockForAction(long id, String ward, BigDecimal quantity) {
    return jdbc.update(
        "INSERT INTO ward_stock(batch_id,ward,quantity) VALUES(?,?,?) ON DUPLICATE KEY UPDATE"
            + " quantity=quantity+VALUES(quantity)",
        id,
        ward,
        quantity);
  }

  public List<Map<String, Object>> findWardStockForWardStock() {
    return jdbc.queryForList(
        "SELECT w.*,b.batch_no,b.expires_on,b.sale_price,b.inventory_item_id,i.name FROM ward_stock"
            + " w JOIN inventory_batches b ON b.id=w.batch_id JOIN inventory_items i ON"
            + " i.id=b.inventory_item_id");
  }

  public Map<String, Object> findAdmissionsForConsume(Long admissionId) {
    return jdbc.queryForMap(
        "SELECT * FROM admissions WHERE id=? AND status='ADMITTED'", admissionId);
  }

  public Map<String, Object> findWardStockForConsume(long id, String ward) {
    return jdbc.queryForMap(
        "SELECT * FROM ward_stock WHERE batch_id=? AND ward=? FOR UPDATE", id, ward);
  }

  public Map<String, Object> findInventoryBatchesForConsume(long id) {
    return jdbc.queryForMap("SELECT * FROM inventory_batches WHERE id=?", id);
  }

  public int updateWardStockForConsume(BigDecimal quantity, long id, String ward) {
    return jdbc.update(
        "UPDATE ward_stock SET quantity=quantity-? WHERE batch_id=? AND ward=?",
        quantity,
        id,
        ward);
  }

  public Map<String, Object> findWardStockForWardAction(long id, String ward) {
    return jdbc.queryForMap(
        "SELECT * FROM ward_stock WHERE batch_id=? AND ward=? FOR UPDATE", id, ward);
  }

  public Map<String, Object> findInventoryBatchesForWardAction(long id) {
    return jdbc.queryForMap("SELECT * FROM inventory_batches WHERE id=? FOR UPDATE", id);
  }

  public int updateWardStockForWardAction(BigDecimal quantity, long id, String ward) {
    return jdbc.update(
        "UPDATE ward_stock SET quantity=quantity-? WHERE batch_id=? AND ward=?",
        quantity,
        id,
        ward);
  }

  public int updateInventoryBatchesForWardAction(BigDecimal quantity, long id) {
    return jdbc.update("UPDATE inventory_batches SET quantity=quantity+? WHERE id=?", quantity, id);
  }

  public int updateInventoryItemsForWardAction(BigDecimal quantity, Object inventoryItemId) {
    return jdbc.update(
        "UPDATE inventory_items SET stock_qty=stock_qty+? WHERE id=?", quantity, inventoryItemId);
  }

  public Map<String, Object> findBatchMovementsForConsumableReturn(Long movementId) {
    return jdbc.queryForMap(
        "SELECT * FROM batch_movements WHERE id=? AND reason='PATIENT_CONSUMABLE' FOR UPDATE",
        movementId);
  }

  public BigDecimal readConsumableReturnsForConsumableReturn(Long movementId) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(quantity),0) FROM consumable_returns WHERE movement_id=?",
        BigDecimal.class,
        movementId);
  }

  public Map<String, Object> findWardStockForConsumableReturn(Object batchId, Object ward) {
    return jdbc.queryForMap(
        "SELECT * FROM ward_stock WHERE batch_id=? AND ward=? FOR UPDATE", batchId, ward);
  }

  public int updateWardStockForConsumableReturn(BigDecimal quantity, Object batchId, Object ward) {
    return jdbc.update(
        "UPDATE ward_stock SET quantity=quantity+? WHERE batch_id=? AND ward=?",
        quantity,
        batchId,
        ward);
  }

  public int insertConsumableReturnsForConsumableReturn(
      Long movementId, BigDecimal quantity, String reason, String name) {
    return jdbc.update(
        "INSERT INTO consumable_returns(movement_id,quantity,reason,performed_by) VALUES(?,?,?,?)",
        movementId,
        quantity,
        reason,
        name);
  }

  public Map<String, Object> findInvoiceItemsForConsumableReturn(Object value1) {
    return jdbc.queryForMap(
        "SELECT invoice_id,unit_price FROM invoice_items WHERE charge_key=?", value1);
  }

  public int insertReturnCreditRequestsForConsumableReturn(
      Object invoiceId, BigDecimal credit, String reason, String name) {
    return jdbc.update(
        "INSERT INTO return_credit_requests(invoice_id,amount,reason,requested_by)"
            + " VALUES(?,?,?,?)",
        invoiceId,
        credit,
        reason,
        name);
  }

  public List<Map<String, Object>> findBatchMovementsForConsumables() {
    return jdbc.queryForList(
        "SELECT m.*,b.batch_no,i.name,p.uhid FROM batch_movements m JOIN inventory_batches b ON"
            + " b.id=m.batch_id JOIN inventory_items i ON i.id=b.inventory_item_id JOIN patients p"
            + " ON p.id=m.patient_id WHERE m.reason='PATIENT_CONSUMABLE' ORDER BY m.id DESC");
  }

  public List<Map<String, Object>> findBatchMovementsForMovements() {
    return jdbc.queryForList(
        "SELECT m.*,b.batch_no,i.name FROM batch_movements m JOIN inventory_batches b ON"
            + " b.id=m.batch_id JOIN inventory_items i ON i.id=b.inventory_item_id ORDER BY m.id"
            + " DESC LIMIT 500");
  }

  public int insertBatchMovementsForMove(
      long batch,
      BigDecimal quantity,
      String reason,
      Long reference,
      Long patient,
      String ward,
      String name,
      String notes) {
    return jdbc.update(
        "INSERT INTO"
            + " batch_movements(batch_id,quantity,reason,reference_id,patient_id,ward,performed_by,notes)"
            + " VALUES(?,?,?,?,?,?,?,?)",
        batch,
        quantity,
        reason,
        reference,
        patient,
        ward,
        name,
        notes);
  }

  public Long readGeneratedIdForId() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }
}
