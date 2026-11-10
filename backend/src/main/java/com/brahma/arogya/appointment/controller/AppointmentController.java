package com.brahma.arogya.appointment.controller;

import com.brahma.arogya.appointment.dto.AppointmentRequest;
import com.brahma.arogya.appointment.dto.AppointmentResponse;
import com.brahma.arogya.appointment.mapper.AppointmentMapper;
import com.brahma.arogya.appointment.service.AppointmentService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

  private final AppointmentService service;

  public AppointmentController(AppointmentService service) {
    this.service = service;
  }

  @GetMapping
  public List<AppointmentResponse> all() {
    return service.all().stream().map(AppointmentMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public AppointmentResponse one(@PathVariable Long id) {
    return AppointmentMapper.toResponse(service.one(id));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping
  public AppointmentResponse create(@RequestBody @jakarta.validation.Valid() AppointmentRequest x) {
    return AppointmentMapper.toResponse(service.create(AppointmentMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PutMapping("/{id}")
  public AppointmentResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() AppointmentRequest x) {
    return AppointmentMapper.toResponse(service.update(id, AppointmentMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
