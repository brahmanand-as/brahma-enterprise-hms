package com.brahma.arogya.hr.service;

import com.brahma.arogya.hr.dto.DoctorProfileRequest;
import com.brahma.arogya.hr.repository.DoctorProfileJdbcRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class DoctorProfileService {

  private final DoctorProfileJdbcRepository db;

  public DoctorProfileService(DoctorProfileJdbcRepository db) {
    this.db = db;
  }

  public List<Map<String, Object>> all() {
    return db.findDoctorProfilesForAll();
  }

  public List<Map<String, Object>> mine(org.springframework.security.core.Authentication auth) {
    return db.findDoctorProfilesForMine(auth.getName());
  }

  public Map<String, Object> create(DoctorProfileRequest x) {
    if (x.employeeId() == null || x.specialty() == null || x.specialty().isBlank())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee and specialty required");
    Integer count = db.countEmployeesForCreate(x.employeeId());
    if (count == null || count == 0)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select an existing employee");
    db.insertDoctorProfilesForCreate(
        x.employeeId(),
        x.specialty(),
        x.qualification(),
        x.experienceYears(),
        x.registrationNumber(),
        x.consultationFee(),
        x.photoUrl(),
        x.biography(),
        x.availabilityNotes());
    return Map.of("status", "CREATED");
  }
}
