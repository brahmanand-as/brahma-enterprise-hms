package com.brahma.arogya.portal.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class EnterprisePortalJdbcRepository {

  private final JdbcTemplate jdbc;

  public EnterprisePortalJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int insertPatientIdentityLocksForRegister(String mobile) {
    return jdbc.update("INSERT IGNORE INTO patient_identity_locks VALUES(?)", mobile);
  }

  public Map<String, Object> findPatientIdentityLocksForRegister(String mobile) {
    return jdbc.queryForMap(
        "SELECT * FROM patient_identity_locks WHERE mobile=? FOR UPDATE", mobile);
  }

  public Integer countPatientsForRegister(String mobile, String firstName, Object value4) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM patients WHERE mobile=? AND LOWER(first_name)=LOWER(?) AND"
            + " LOWER(COALESCE(last_name,''))=LOWER(?)",
        Integer.class,
        mobile,
        firstName,
        value4);
  }

  public int insertStaffAccountsForRegister(String username, Object value2, Long id) {
    return jdbc.update(
        "INSERT INTO staff_accounts(username,password_hash,role,patient_id)"
            + " VALUES(?,?,'PATIENT',?)",
        username,
        value2,
        id);
  }

  public int updatePatientsForUpdateProfile(
      String mobile, String address, String bloodGroup, long value4) {
    return jdbc.update(
        "UPDATE patients SET mobile=?,address=?,blood_group=? WHERE id=?",
        mobile,
        address,
        bloodGroup,
        value4);
  }

  public Map<String, Object> findPatientsForProfile(long value1) {
    return jdbc.queryForMap("SELECT * FROM patients WHERE id=?", value1);
  }

  public List<Map<String, Object>> findAppointmentsForAppointments(long value1) {
    return jdbc.queryForList(
        "SELECT * FROM appointments WHERE patient_id=? ORDER BY appointment_at DESC", value1);
  }

  public List<Map<String, Object>> findInvoicesForInvoices(long value1) {
    return jdbc.queryForList(
        "SELECT * FROM invoices WHERE patient_id=? ORDER BY created_at DESC", value1);
  }

  public List<Map<String, Object>> findPrescriptionsForPrescriptions(long value1) {
    return jdbc.queryForList(
        "SELECT pr.id,pr.created_at,i.medicine_name,i.dose,i.frequency,i.duration,i.instructions"
            + " FROM prescriptions pr JOIN prescription_items i ON i.prescription_id=pr.id WHERE"
            + " pr.patient_id=? AND pr.status='FINAL' ORDER BY pr.created_at DESC",
        value1);
  }

  public List<Map<String, Object>> findLabOrderItemsForReports(long value1) {
    return jdbc.queryForList(
        "SELECT"
            + " i.test_name,i.result_value,i.result_unit,i.reference_range,i.verified_by,i.verified_at"
            + " FROM lab_order_items i JOIN lab_orders o ON o.id=i.lab_order_id WHERE"
            + " o.patient_id=? AND i.status IN ('VERIFIED','COMPLETED')",
        value1);
  }

  public int insertRegistrationPaymentsForFee(long pid) {
    return jdbc.update(
        "INSERT INTO registration_payments(patient_id,amount,method,status) SELECT"
            + " ?,CAST(setting_value AS DECIMAL(12,2)),'DUMMY_ONLINE','COMPLETED' FROM"
            + " hospital_settings WHERE setting_key='registration_fee' ON DUPLICATE KEY UPDATE"
            + " patient_id=VALUES(patient_id)",
        pid);
  }

  public List<Map<String, Object>> findHelpdeskTicketsForTickets() {
    return jdbc.queryForList("SELECT * FROM helpdesk_tickets ORDER BY updated_at DESC");
  }

  public List<Map<String, Object>> findHelpdeskTicketsForTickets2(String name) {
    return jdbc.queryForList(
        "SELECT * FROM helpdesk_tickets WHERE created_by=? ORDER BY updated_at DESC", name);
  }

  public int insertHelpdeskTicketsForTicket(
      String subject, String description, String priority, String name) {
    return jdbc.update(
        "INSERT INTO helpdesk_tickets(subject,description,priority,created_by) VALUES(?,?,?,?)",
        subject,
        description,
        priority,
        name);
  }

  public int insertHelpdeskTicketsForTicket2() {
    return jdbc.update(
        "UPDATE helpdesk_tickets SET due_at=DATE_ADD(created_at,INTERVAL CASE priority WHEN"
            + " 'URGENT' THEN 1 WHEN 'HIGH' THEN 4 ELSE 24 END HOUR) WHERE id=LAST_INSERT_ID()");
  }

  public int updateHelpdeskTicketsForReview(
      String status, String assignedTo, String resolution, String department, long id) {
    return jdbc.update(
        "UPDATE helpdesk_tickets SET"
            + " status=?,assigned_to=?,resolution=?,department=?,updated_at=NOW() WHERE id=?",
        status,
        assignedTo,
        resolution,
        department,
        id);
  }

  public BigDecimal readPaymentsForAnalytics(LocalDate start, LocalDate end) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(amount),0) FROM payments WHERE paid_at>=? AND paid_at<?",
        BigDecimal.class,
        start,
        end);
  }

  public BigDecimal readPaymentsForAnalytics2(LocalDate previous, LocalDate start) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(amount),0) FROM payments WHERE paid_at>=? AND paid_at<?",
        BigDecimal.class,
        previous,
        start);
  }

  public Long countPatientsForAnalytics(LocalDate start, LocalDate end) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM patients WHERE created_at>=? AND created_at<?",
        Long.class,
        start,
        end);
  }

  public Long countEncountersForAnalytics(LocalDate start, LocalDate end) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM encounters WHERE started_at>=? AND started_at<?",
        Long.class,
        start,
        end);
  }
}
