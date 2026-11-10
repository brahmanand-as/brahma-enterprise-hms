package com.brahma.arogya.administration.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class HospitalMasterJdbcRepository {

  private final JdbcTemplate jdbc;

  public HospitalMasterJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findDepartmentsForDepartments() {
    return jdbc.queryForList("SELECT * FROM departments ORDER BY name");
  }

  public int insertDepartmentsForCreate(String code, String name) {
    return jdbc.update("INSERT INTO departments(code,name) VALUES(?,?)", code, name);
  }
}
