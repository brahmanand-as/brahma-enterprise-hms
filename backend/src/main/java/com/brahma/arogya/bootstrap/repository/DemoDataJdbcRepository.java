package com.brahma.arogya.bootstrap.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class DemoDataJdbcRepository {

  private final JdbcTemplate jdbc;

  public DemoDataJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Integer countPatientsForRun() {
    return jdbc.queryForObject("SELECT COUNT(*) FROM patients", Integer.class);
  }

  public int updateIpdBedsForRun() {
    return jdbc.update("UPDATE ipd_beds SET bed_type='ICU' WHERE ward='ICU'");
  }

  public int insertPatientsForRun() {
    return jdbc.update(
        "INSERT INTO patients(uhid,first_name,last_name,mobile)"
            + " VALUES('MRN-00000001','Synthetic','Patient','0000000000')");
  }

  public Long readGeneratedIdForRun() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public int updatePatientNumberSequenceForRun() {
    return jdbc.update("UPDATE patient_number_sequence SET next_value=2 WHERE id=1");
  }

  public int insertStaffAccountsForRun(String demoHash, long pid) {
    return jdbc.update(
        "INSERT INTO staff_accounts(username,password_hash,role,patient_id)"
            + " VALUES('demo.patient',?,'PATIENT',?)",
        demoHash,
        pid);
  }

  public int insertEmployeesForRun(String value1, String name, String role) {
    return jdbc.update(
        "INSERT INTO employees(employee_no,name,department,designation,status)"
            + " VALUES(?,?,'DEMO',?,'ACTIVE')",
        value1,
        name,
        role);
  }

  public Long readGeneratedIdForRun2() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public int insertStaffAccountsForRun2(String value1, String demoHash, String role, long eid) {
    return jdbc.update(
        "INSERT INTO staff_accounts(username,password_hash,role,employee_id) VALUES(?,?,?,?)",
        value1,
        demoHash,
        role,
        eid);
  }

  public int updateEmployeeNumberSequenceForRun(int seq) {
    return jdbc.update("UPDATE employee_number_sequence SET next_value=? WHERE id=1", seq);
  }

  public int insertAppointmentSlotsForRun(LocalDateTime t, LocalDateTime value2) {
    return jdbc.update(
        "INSERT INTO appointment_slots(doctor_name,starts_at,ends_at) VALUES('Dr Synthetic"
            + " Demo',?,?)",
        t,
        value2);
  }

  public int insertInventoryItemsForRun(LocalDate value1) {
    return jdbc.update(
        "INSERT INTO"
            + " inventory_items(sku,name,category,stock_qty,reorder_level,unit,batch_no,expires_on)"
            + " VALUES('DEMO-001','Synthetic stock — not for clinical"
            + " use','DEMO',100,10,'UNIT','SYNTHETIC',?)",
        value1);
  }

  public int insertDutyRosterForRun(LocalDate value1, LocalDate value2) {
    return jdbc.update(
        "INSERT INTO duty_roster(employee_id,shift_date,starts_at,ends_at,ends_on) SELECT"
            + " id,?,'09:00','13:00',? FROM employees WHERE name='Dr Synthetic Demo'",
        value1,
        value2);
  }

  public int updateAppointmentSlotsForRun() {
    return jdbc.update(
        "UPDATE appointment_slots SET doctor_employee_id=(SELECT id FROM employees WHERE name='Dr"
            + " Synthetic Demo' LIMIT 1)");
  }

  public int insertCareTeamsForRun(long pid) {
    return jdbc.update("INSERT INTO care_teams(patient_id,username) VALUES(?,'demo.nurse')", pid);
  }

  public int insertInventoryBatchesForRun2() {
    return jdbc.update(
        "INSERT INTO"
            + " inventory_batches(inventory_item_id,batch_no,expires_on,quantity,unit_cost,sale_price)"
            + " SELECT id,'SYNTHETIC',DATE_ADD(CURDATE(),INTERVAL 1 YEAR),stock_qty,5,10 FROM"
            + " inventory_items");
  }

  public int insertExternalLabsForRun() {
    return jdbc.update(
        "INSERT INTO external_labs(name,contact,active) VALUES('Synthetic Reference"
            + " Laboratory','DEMO',1)");
  }
}
