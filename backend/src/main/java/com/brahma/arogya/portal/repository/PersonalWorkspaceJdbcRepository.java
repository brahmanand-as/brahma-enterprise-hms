package com.brahma.arogya.portal.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class PersonalWorkspaceJdbcRepository {

  private final JdbcTemplate jdbc;

  public PersonalWorkspaceJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findNotificationsForNotifications(String name) {
    return jdbc.queryForList(
        "SELECT * FROM notifications WHERE username=? ORDER BY id DESC LIMIT 200", name);
  }

  public int updateNotificationsForRead(long id, String name) {
    return jdbc.update("UPDATE notifications SET is_read=1 WHERE id=? AND username=?", id, name);
  }

  public String readStaffAccountsForPassword(String name) {
    return jdbc.queryForObject(
        "SELECT password_hash FROM staff_accounts WHERE username=?", String.class, name);
  }

  public int updateStaffAccountsForPassword(Object value1, String name) {
    return jdbc.update(
        "UPDATE staff_accounts SET password_hash=?,token_version=token_version+1 WHERE username=?",
        value1,
        name);
  }

  public int updateStaffAccountsForLogout(String name) {
    return jdbc.update(
        "UPDATE staff_accounts SET token_version=token_version+1 WHERE username=?", name);
  }
}
