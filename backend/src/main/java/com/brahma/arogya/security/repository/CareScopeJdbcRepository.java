package com.brahma.arogya.security.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class CareScopeJdbcRepository {

  private final JdbcTemplate jdbc;

  public CareScopeJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Integer countCareTeamsForCan(
      long patient, String u, long patient2, String u2, long patient3, String u3) {
    return jdbc.queryForObject(
        "SELECT (SELECT COUNT(*) FROM care_teams WHERE patient_id=? AND username=?)+(SELECT"
            + " COUNT(*) FROM encounters e JOIN staff_accounts s ON"
            + " s.employee_id=e.doctor_employee_id WHERE e.patient_id=? AND"
            + " s.username=?)+(SELECT COUNT(*) FROM appointments a JOIN staff_accounts s ON"
            + " s.employee_id=a.doctor_employee_id WHERE a.patient_id=? AND s.username=?)",
        Integer.class,
        patient,
        u,
        patient2,
        u2,
        patient3,
        u3);
  }

  public String readEmployeesForDoctorName(long id) {
    return jdbc.queryForObject(
        "SELECT name FROM employees WHERE id=? AND designation='DOCTOR' AND status='ACTIVE'",
        String.class,
        id);
  }

  public List<Long> findDepartmentsForDepartmentId(long employee) {
    return jdbc.queryForList(
        "SELECT d.id FROM departments d JOIN employees e ON e.department=d.name WHERE e.id=?",
        Long.class,
        employee);
  }

  public List<Long> findEmployeesForDoctorId(String name) {
    return jdbc.queryForList(
        "SELECT DISTINCT e.id FROM employees e JOIN staff_accounts s ON s.employee_id=e.id JOIN"
            + " role_permissions p ON p.role_name=s.role WHERE e.name=? AND e.status='ACTIVE'"
            + " AND e.designation='DOCTOR' AND p.permission_code='clinical.consult' AND"
            + " s.enabled=1",
        Long.class,
        name);
  }

  public Long readEncountersForEncounter(long id) {
    return jdbc.queryForObject("SELECT patient_id FROM encounters WHERE id=?", Long.class, id);
  }

  public Long readAdmissionsForAdmission(long id) {
    return jdbc.queryForObject("SELECT patient_id FROM admissions WHERE id=?", Long.class, id);
  }

  public Long readEncountersForVisible(Long encounterId) {
    return jdbc.queryForObject(
        "SELECT patient_id FROM encounters WHERE id=?", Long.class, encounterId);
  }

  public Long readEncountersForVisible2(Long encounterId) {
    return jdbc.queryForObject(
        "SELECT patient_id FROM encounters WHERE id=?", Long.class, encounterId);
  }

  public Long readAdmissionsForVisible(long value2) {
    return jdbc.queryForObject("SELECT patient_id FROM admissions WHERE id=?", Long.class, value2);
  }
}
