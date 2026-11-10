package com.brahma.arogya.helpdesk.controller;

import com.brahma.arogya.helpdesk.dto.HelpdeskConversationMessageRequest;
import com.brahma.arogya.helpdesk.service.HelpdeskConversationService;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/helpdesk")
public class HelpdeskConversationController {

  private final HelpdeskConversationService service;

  public HelpdeskConversationController(HelpdeskConversationService service) {
    this.service = service;
  }

  @GetMapping("/tickets/{id}/messages")
  public List<Map<String, Object>> messages(@PathVariable long id, Authentication a) {
    return service.messages(id, a);
  }

  @PostMapping("/tickets/{id}/messages")
  public void message(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() HelpdeskConversationMessageRequest f,
      Authentication a) {
    service.message(id, f, a);
  }
}
