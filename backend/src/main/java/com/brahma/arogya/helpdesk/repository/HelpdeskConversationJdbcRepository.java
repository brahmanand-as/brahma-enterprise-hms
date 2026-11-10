package com.brahma.arogya.helpdesk.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class HelpdeskConversationJdbcRepository {

  private final JdbcTemplate jdbc;

  public HelpdeskConversationJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> findHelpdeskTicketsForTicket(long id) {
    return jdbc.queryForMap("SELECT * FROM helpdesk_tickets WHERE id=?", id);
  }

  public List<Map<String, Object>> findHelpdeskMessagesForMessages(long id) {
    return jdbc.queryForList("SELECT * FROM helpdesk_messages WHERE ticket_id=? ORDER BY id", id);
  }

  public int insertHelpdeskMessagesForMessage(long id, String name, String message) {
    return jdbc.update(
        "INSERT INTO helpdesk_messages(ticket_id,username,message) VALUES(?,?,?)",
        id,
        name,
        message);
  }
}
