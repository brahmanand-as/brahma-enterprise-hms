package com.brahma.arogya.config.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class SecurityConfigJdbcRepository {

  private final JdbcTemplate jdbc;

  public SecurityConfigJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Integer countStaffAccountsForUsers(String admin) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM staff_accounts WHERE username=?", Integer.class, admin);
  }

  public int insertStaffAccountsForUsers(String admin, String value2, String value3) {
    return jdbc.update(
        "INSERT INTO staff_accounts(username,password_hash,role,enabled) VALUES (?,?,?,1)",
        admin,
        value2,
        value3);
  }

  public List<Map<String, Object>> findStaffAccountsForUsers(String username) {
    return jdbc.queryForList(
        "SELECT password_hash,role,enabled FROM staff_accounts WHERE username=?", username);
  }

  public List<String> findRolePermissionsForUsers(Object role) {
    return jdbc.query(
        "SELECT permission_code FROM role_permissions WHERE role_name=?",
        (rs, n) -> "PERM_" + rs.getString(1),
        role);
  }
}
