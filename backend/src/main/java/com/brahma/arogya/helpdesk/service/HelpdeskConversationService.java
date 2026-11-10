package com.brahma.arogya.helpdesk.service;

import com.brahma.arogya.helpdesk.dto.HelpdeskConversationMessageRequest;
import com.brahma.arogya.helpdesk.repository.HelpdeskConversationJdbcRepository;
import com.brahma.arogya.notification.service.CoordinationService;
import com.brahma.arogya.security.service.AccessPolicy;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class HelpdeskConversationService {

  private final HelpdeskConversationJdbcRepository db;

  private final AccessPolicy access;

  private final CoordinationService notices;

  public HelpdeskConversationService(
      HelpdeskConversationJdbcRepository db, AccessPolicy access, CoordinationService notices) {
    this.db = db;
    this.access = access;
    this.notices = notices;
  }

  private Map<String, Object> ticket(long id, Authentication a) {
    var t = db.findHelpdeskTicketsForTicket(id);
    if (!access.has(a, "helpdesk.manage") && !t.get("created_by").equals(a.getName()))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    return t;
  }

  public List<Map<String, Object>> messages(long id, Authentication a) {
    ticket(id, a);
    return db.findHelpdeskMessagesForMessages(id);
  }

  public void message(long id, HelpdeskConversationMessageRequest f, Authentication a) {
    var t = ticket(id, a);
    if (f.message() == null || f.message().isBlank() || f.message().length() > 2000)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Message of up to 2000 characters required");
    if ("CLOSED".equals(t.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Closed ticket");
    db.insertHelpdeskMessagesForMessage(id, a.getName(), f.message());
    if (!t.get("created_by").equals(a.getName()))
      notices.notify(String.valueOf(t.get("created_by")), "Helpdesk response", f.message());
  }
}
