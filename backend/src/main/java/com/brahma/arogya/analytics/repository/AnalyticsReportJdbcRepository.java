package com.brahma.arogya.analytics.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class AnalyticsReportJdbcRepository {

  private final JdbcTemplate jdbc;

  public AnalyticsReportJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> countEncountersForOverview(LocalDate start, LocalDate value2) {
    return jdbc.queryForList(
        "SELECT COALESCE(d.name,'General / unassigned') department,COUNT(e.id)"
            + " visits,COALESCE(AVG(TIMESTAMPDIFF(MINUTE,e.started_at,e.consultation_at)),0)"
            + " average_wait_minutes FROM encounters e LEFT JOIN departments d ON"
            + " d.id=e.department_id WHERE e.started_at>=? AND e.started_at<? GROUP BY"
            + " COALESCE(d.name,'General / unassigned')",
        start,
        value2);
  }

  public List<Map<String, Object>> findPaymentsForOverview(LocalDate start, LocalDate value2) {
    return jdbc.queryForList(
        "SELECT DATE(paid_at) day,SUM(amount) collections FROM payments WHERE paid_at>=? AND"
            + " paid_at<? GROUP BY DATE(paid_at) ORDER BY day",
        start,
        value2);
  }

  public Long countPatientsForMetrics(LocalDate start, LocalDate end) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM patients WHERE created_at>=? AND created_at<?",
        Long.class,
        start,
        end);
  }

  public Long countEncountersForMetrics(
      LocalDate start, LocalDate end, String department, String department2) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM encounters e LEFT JOIN departments d ON d.id=e.department_id"
            + " WHERE e.started_at>=? AND e.started_at<? AND (?='' OR d.name=?)",
        Long.class,
        start,
        end,
        department,
        department2);
  }

  public BigDecimal readDepartmentsForMetrics(
      LocalDate start, LocalDate end, String department, String department2) {
    return jdbc.queryForObject(
        "SELECT COALESCE(AVG(TIMESTAMPDIFF(MINUTE,e.started_at,e.consultation_at)),0) FROM"
            + " encounters e LEFT JOIN departments d ON d.id=e.department_id WHERE"
            + " e.started_at>=? AND e.started_at<? AND (?='' OR d.name=?)",
        BigDecimal.class,
        start,
        end,
        department,
        department2);
  }

  public BigDecimal readPaymentsForMetrics(
      LocalDate start, LocalDate end, String department, String department2) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(p.amount),0) FROM payments p JOIN invoices i ON"
            + " i.id=p.invoice_id LEFT JOIN encounters e ON e.id=i.encounter_id LEFT JOIN"
            + " departments d ON d.id=COALESCE(i.department_id,e.department_id) WHERE"
            + " p.paid_at>=? AND p.paid_at<? AND (?='' OR d.name=?)",
        BigDecimal.class,
        start,
        end,
        department,
        department2);
  }

  public BigDecimal readRefundsForMetrics(
      LocalDate start, LocalDate end, String department, String department2) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(r.amount),0) FROM refunds r JOIN invoices i ON"
            + " i.id=r.invoice_id LEFT JOIN encounters e ON e.id=i.encounter_id LEFT JOIN"
            + " departments d ON d.id=COALESCE(i.department_id,e.department_id) WHERE"
            + " r.created_at>=? AND r.created_at<? AND (?='' OR d.name=?)",
        BigDecimal.class,
        start,
        end,
        department,
        department2);
  }

  public BigDecimal readInvoicesForMetrics(
      LocalDate start, LocalDate end, String department, String department2) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(i.total),0) FROM invoices i LEFT JOIN encounters e ON"
            + " e.id=i.encounter_id LEFT JOIN departments d ON"
            + " d.id=COALESCE(i.department_id,e.department_id) WHERE i.created_at>=? AND"
            + " i.created_at<? AND (?='' OR d.name=?)",
        BigDecimal.class,
        start,
        end,
        department,
        department2);
  }

  public Long countMedicationAdministrationsForMetrics(LocalDate start, LocalDate end) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM medication_administrations WHERE status='MISSED' AND"
            + " scheduled_at>=? AND scheduled_at<?",
        Long.class,
        start,
        end);
  }

  public Long countCareQualityEventsForMetrics(
      LocalDate start, LocalDate end, String department, String department2) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM care_quality_events WHERE created_at>=? AND created_at<? AND"
            + " (?='' OR department=?)",
        Long.class,
        start,
        end,
        department,
        department2);
  }

  public Long countAdmissionsForMetrics(LocalDate start, LocalDate end) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM admissions WHERE admitted_at>=? AND admitted_at<?",
        Long.class,
        start,
        end);
  }

  public int insertCareQualityEventsForQuality(
      Long encounterId,
      String department,
      String eventType,
      String severity,
      String description,
      String name) {
    return jdbc.update(
        "INSERT INTO"
            + " care_quality_events(encounter_id,department,event_type,severity,description,recorded_by)"
            + " VALUES(?,?,?,?,?,?)",
        encounterId,
        department,
        eventType,
        severity,
        description,
        name);
  }
}
