package com.brahma.arogya.portal.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class PortalDataJdbcRepository {

  private final JdbcTemplate jdbc;

  public PortalDataJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findPatientsForOptions() {
    return jdbc.queryForList(
        "SELECT id,id patient_id,uhid,CONCAT(first_name,' ',COALESCE(last_name,'')) label"
            + " FROM patients ORDER BY id DESC LIMIT 1000");
  }

  public List<Map<String, Object>> findEncountersForOptions() {
    return jdbc.queryForList(
        "SELECT id,patient_id,CONCAT(encounter_no,' / ',status) label,status FROM"
            + " encounters ORDER BY id DESC LIMIT 1000");
  }

  public List<Map<String, Object>> findAdmissionsForOptions() {
    return jdbc.queryForList(
        "SELECT a.id,a.patient_id,CONCAT(a.admission_no,' / ',p.first_name,' / ',a.ward,'"
            + " ',a.bed_no) label FROM admissions a JOIN patients p ON p.id=a.patient_id"
            + " WHERE a.status='ADMITTED'");
  }

  public List<Map<String, Object>> findEmployeesForOptions() {
    return jdbc.queryForList(
        "SELECT id,CONCAT(employee_no,' / ',name) label FROM employees WHERE status='ACTIVE'"
            + " ORDER BY name");
  }

  public List<Map<String, Object>> findStaffAccountsForOptions() {
    return jdbc.queryForList(
        "SELECT DISTINCT a.username id,a.username label FROM staff_accounts a JOIN"
            + " role_permissions p ON p.role_name=a.role WHERE p.permission_code IN"
            + " ('clinical.consult','nursing.write') AND a.enabled=1");
  }

  public List<Map<String, Object>> findEmployeesForOptions2() {
    return jdbc.queryForList(
        "SELECT DISTINCT e.id,e.name label FROM employees e JOIN staff_accounts a ON"
            + " a.employee_id=e.id JOIN role_permissions p ON p.role_name=a.role WHERE"
            + " p.permission_code='clinical.consult' AND e.designation='DOCTOR' AND"
            + " a.enabled=1 AND e.status='ACTIVE' ORDER BY e.name");
  }

  public List<Map<String, Object>> findIpdBedsForOptions() {
    return jdbc.queryForList(
        "SELECT id,CONCAT(ward,' / ',bed_no,' / ',status) label,status FROM ipd_beds");
  }

  public List<Map<String, Object>> findInventoryItemsForOptions() {
    return jdbc.queryForList(
        "SELECT id,CONCAT(sku,' / ',name) label,name FROM inventory_items ORDER BY name");
  }

  public List<Map<String, Object>> findLabTestCatalogForOptions2() {
    return jdbc.queryForList(
        "SELECT code id,CONCAT(name,' / ',price) label,default_processing_type FROM"
            + " lab_test_catalog WHERE active=1");
  }

  public List<Map<String, Object>> findExternalLabsForOptions() {
    return jdbc.queryForList("SELECT id,name label FROM external_labs WHERE active=1");
  }

  public List<Map<String, Object>> findInvoicesForOptions() {
    return jdbc.queryForList(
        "SELECT id,CONCAT(invoice_no,' / balance ',total-paid) label FROM invoices ORDER BY"
            + " id DESC LIMIT 1000");
  }

  public List<Map<String, Object>> findStaffAccountsForOptions2() {
    return jdbc.queryForList(
        "SELECT username id,username label FROM staff_accounts WHERE enabled=1 AND"
            + " role<>'PATIENT' ORDER BY username");
  }

  public List<Map<String, Object>> findEnterpriseRolesForOptions() {
    return jdbc.queryForList(
        "SELECT name id,label FROM enterprise_roles WHERE name<>'PATIENT' ORDER BY name");
  }

  public List<Map<String, Object>> findDepartmentsForOptions() {
    return jdbc.queryForList("SELECT id,name label FROM departments ORDER BY name");
  }
}
