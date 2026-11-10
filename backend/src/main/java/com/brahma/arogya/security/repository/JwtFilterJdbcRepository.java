package com.brahma.arogya.security.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class JwtFilterJdbcRepository {

  private final JdbcTemplate jdbc;

  public JwtFilterJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Integer readStaffAccountsForDoFilterInternal(String username) {
    return jdbc.queryForObject(
        "SELECT token_version FROM staff_accounts WHERE username=?", Integer.class, username);
  }
}
