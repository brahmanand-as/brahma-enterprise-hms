package com.brahma.arogya.hr.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class DoctorProfileJdbcRepository {

  private final JdbcTemplate jdbc;

  public DoctorProfileJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findDoctorProfilesForAll() {
    return jdbc.queryForList(
        "SELECT d.*,e.name AS doctor_name,e.department FROM doctor_profiles d JOIN employees e ON"
            + " e.id=d.employee_id ORDER BY e.name");
  }

  public List<Map<String, Object>> findDoctorProfilesForMine(String name) {
    return jdbc.queryForList(
        "SELECT d.*,e.name AS doctor_name FROM doctor_profiles d JOIN employees e ON"
            + " e.id=d.employee_id JOIN staff_accounts s ON s.employee_id=e.id WHERE s.username=?",
        name);
  }

  public Integer countEmployeesForCreate(Long employeeId) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM employees WHERE id=?", Integer.class, employeeId);
  }

  public int insertDoctorProfilesForCreate(
      Long employeeId,
      String specialty,
      String qualification,
      Integer experienceYears,
      String registrationNumber,
      BigDecimal consultationFee,
      String photoUrl,
      String biography,
      String availabilityNotes) {
    return jdbc.update(
        "INSERT INTO"
            + " doctor_profiles(employee_id,specialty,qualification,experience_years,registration_number,consultation_fee,photo_url,biography,availability_notes)"
            + " VALUES(?,?,?,?,?,?,?,?,?)",
        employeeId,
        specialty,
        qualification,
        experienceYears,
        registrationNumber,
        consultationFee,
        photoUrl,
        biography,
        availabilityNotes);
  }
}
