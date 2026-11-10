package com.brahma.arogya.patient.service;

import com.brahma.arogya.patient.dto.ReturningPatientAccessRequest;
import com.brahma.arogya.patient.repository.ReturningPatientAccessJdbcRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class ReturningPatientAccessService {

  private final ReturningPatientAccessJdbcRepository db;

  private final PasswordEncoder encoder;

  public ReturningPatientAccessService(
      ReturningPatientAccessJdbcRepository db, PasswordEncoder encoder) {
    this.db = db;
    this.encoder = encoder;
  }

  public Map<String, String> link(ReturningPatientAccessRequest f) {
    if (f.patientId() == null
        || f.username() == null
        || !f.username().matches("[A-Za-z0-9._-]{3,80}")
        || f.password() == null
        || f.password().length() < 12
        || !Boolean.TRUE.equals(f.identityVerified()))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Verified patient identity, account name and 12+ character temporary password required");
    db.findPatientsForLink(f.patientId());
    if (db.countStaffAccountsForLink(f.patientId()) > 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Patient already has portal access; reset existing credentials");
    db.insertStaffAccountsForLink(f.username(), encoder.encode(f.password()), f.patientId());
    return Map.of("status", "LINKED_TO_EXISTING_MRN");
  }
}
