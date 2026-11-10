package com.brahma.arogya.administration.service;

import com.brahma.arogya.administration.dto.HospitalMasterDepartmentRequest;
import com.brahma.arogya.administration.repository.HospitalMasterJdbcRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class HospitalMasterService {

  private final HospitalMasterJdbcRepository db;

  public HospitalMasterService(HospitalMasterJdbcRepository db) {
    this.db = db;
  }

  public List<Map<String, Object>> departments() {
    return db.findDepartmentsForDepartments();
  }

  public Map<String, String> create(HospitalMasterDepartmentRequest f) {
    if (f.code() == null
        || !f.code().matches("[A-Z][A-Z0-9_]{1,29}")
        || f.name() == null
        || f.name().isBlank())
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Unique department code and name required");
    db.insertDepartmentsForCreate(f.code(), f.name());
    return Map.of("status", "CREATED");
  }
}
