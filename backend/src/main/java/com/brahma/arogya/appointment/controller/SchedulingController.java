package com.brahma.arogya.appointment.controller;

import com.brahma.arogya.appointment.dto.SchedulingBookingRequest;
import com.brahma.arogya.appointment.dto.SchedulingRescheduleRequest;
import com.brahma.arogya.appointment.dto.SchedulingSlotRequest;
import com.brahma.arogya.appointment.service.SchedulingService;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SchedulingController {

  private final SchedulingService service;

  public SchedulingController(SchedulingService service) {
    this.service = service;
  }

  @GetMapping({"/public/slots", "/scheduling/slots", "/patient/slots"})
  public List<Map<String, Object>> slots() {
    return service.slots();
  }

  @PostMapping("/scheduling/slots")
  public Map<String, String> slot(
      @RequestBody @jakarta.validation.Valid() SchedulingSlotRequest r) {
    return service.slot(r);
  }

  @PostMapping({"/scheduling/book", "/patient/book"})
  public Map<String, Object> book(
      @RequestBody @jakarta.validation.Valid() SchedulingBookingRequest r,
      Principal p,
      jakarta.servlet.http.HttpServletRequest req) {
    return service.book(r, p, req);
  }

  @PostMapping({
    "/patient/appointments/{id}/reschedule",
    "/scheduling/appointments/{id}/reschedule"
  })
  public Map<String, String> reschedule(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() SchedulingRescheduleRequest r,
      Principal p,
      jakarta.servlet.http.HttpServletRequest req) {
    return service.reschedule(id, r, p, req);
  }

  @PostMapping("/patient/appointments/{id}/reschedule-request")
  public Map<String, String> request(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() SchedulingRescheduleRequest r,
      Principal p) {
    return service.request(id, r, p);
  }

  @GetMapping("/scheduling/requests")
  public List<Map<String, Object>> requests() {
    return service.requests();
  }
}
