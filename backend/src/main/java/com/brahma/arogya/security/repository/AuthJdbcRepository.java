package com.brahma.arogya.security.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class AuthJdbcRepository {

  private final JdbcTemplate jdbc;

  public AuthJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Integer readStaffAccountsForLogin(Object username) {
    return jdbc.queryForObject(
        "SELECT token_version FROM staff_accounts WHERE username=?", Integer.class, username);
  }

  public List<Map<String, Object>> findStaffAccountsForMe(String name) {
    return jdbc.queryForList("SELECT role,employee_id FROM staff_accounts WHERE username=?", name);
  }
}
