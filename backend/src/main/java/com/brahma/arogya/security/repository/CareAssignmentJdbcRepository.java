package com.brahma.arogya.security.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class CareAssignmentJdbcRepository {

  private final JdbcTemplate jdbc;

  public CareAssignmentJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findCareTeamsForTeams() {
    return jdbc.queryForList(
        "SELECT c.*,p.uhid,p.first_name FROM care_teams c JOIN patients p ON p.id=c.patient_id");
  }

  public Integer countStaffAccountsForAssign(String username) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM staff_accounts a JOIN role_permissions p ON"
            + " p.role_name=a.role WHERE a.username=? AND p.permission_code IN"
            + " ('clinical.consult','nursing.write') AND a.enabled=1",
        Integer.class,
        username);
  }

  public int insertCareTeamsForAssign(Long patientId, String username) {
    return jdbc.update("INSERT IGNORE INTO care_teams VALUES(?,?)", patientId, username);
  }

  public int deleteCareTeamsForRemove(long patientId, String username) {
    return jdbc.update(
        "DELETE FROM care_teams WHERE patient_id=? AND username=?", patientId, username);
  }
}
