package com.brahma.arogya.security.service;

import com.brahma.arogya.security.dto.CareAssignmentRequest;
import com.brahma.arogya.security.repository.CareAssignmentJdbcRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class CareAssignmentService {

  private final CareAssignmentJdbcRepository db;

  public CareAssignmentService(CareAssignmentJdbcRepository db) {
    this.db = db;
  }

  public List<Map<String, Object>> teams() {
    return db.findCareTeamsForTeams();
  }

  public void assign(CareAssignmentRequest f) {
    if (f.patientId() == null
        || f.username() == null
        || db.countStaffAccountsForAssign(f.username()) == 0)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Linked clinical staff and patient required");
    db.insertCareTeamsForAssign(f.patientId(), f.username());
  }

  public void remove(long patientId, String username) {
    db.deleteCareTeamsForRemove(patientId, username);
  }
}
