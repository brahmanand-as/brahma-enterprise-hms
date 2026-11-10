package com.brahma.arogya.appointment.service;

import com.brahma.arogya.appointment.entity.Appointment;
import com.brahma.arogya.appointment.repository.AppointmentRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class AppointmentService {

  private final AppointmentRepository r;

  public AppointmentService(AppointmentRepository r) {
    this.r = r;
  }

  public List<Appointment> all() {
    return r.findAll();
  }

  public Appointment one(Long id) {
    return r.findById(id).orElseThrow();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Appointment create(Appointment x) {
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Appointment update(Long id, Appointment x) {
    x.setId(id);
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
