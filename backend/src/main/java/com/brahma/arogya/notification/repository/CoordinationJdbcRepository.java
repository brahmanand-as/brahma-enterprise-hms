package com.brahma.arogya.notification.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class CoordinationJdbcRepository {

  private final JdbcTemplate jdbc;

  public CoordinationJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int insertNotificationsForNotify(String username, String subject, String message) {
    return jdbc.update(
        "INSERT INTO notifications(username,subject,message) VALUES(?,?,?)",
        username,
        subject,
        message);
  }

  public List<String> findStaffAccountsForPatient(long id) {
    return jdbc.queryForList(
        "SELECT username FROM staff_accounts WHERE patient_id=?", String.class, id);
  }

  public List<String> findStaffAccountsForDepartment(String permission) {
    return jdbc.queryForList(
        "SELECT DISTINCT a.username FROM staff_accounts a JOIN role_permissions p ON"
            + " p.role_name=a.role WHERE p.permission_code=? AND a.enabled=1",
        String.class,
        permission);
  }
}
