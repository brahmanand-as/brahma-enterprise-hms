package com.brahma.arogya.clinical.service;

import com.brahma.arogya.appointment.entity.Appointment;
import com.brahma.arogya.appointment.repository.AppointmentRepository;
import com.brahma.arogya.clinical.dto.ClinicalWorkflowWalkInRequest;
import com.brahma.arogya.clinical.entity.Consultation;
import com.brahma.arogya.clinical.entity.Encounter;
import com.brahma.arogya.clinical.entity.Vital;
import com.brahma.arogya.clinical.repository.ClinicalWorkflowJdbcRepository;
import com.brahma.arogya.clinical.repository.ConsultationRepository;
import com.brahma.arogya.clinical.repository.EncounterRepository;
import com.brahma.arogya.clinical.repository.VitalRepository;
import com.brahma.arogya.patient.repository.PatientRepository;
import com.brahma.arogya.security.service.CareScope;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class ClinicalWorkflowService {

  private final CareScope care;

  private final ClinicalWorkflowJdbcRepository db;

  private final AppointmentRepository appointments;

  private final EncounterRepository encounters;

  private final PatientRepository patients;

  private final VitalRepository vitals;

  private final ConsultationRepository consultations;

  public ClinicalWorkflowService(
      AppointmentRepository appointments,
      EncounterRepository encounters,
      PatientRepository patients,
      VitalRepository vitals,
      ConsultationRepository consultations,
      CareScope care,
      ClinicalWorkflowJdbcRepository db) {
    this.appointments = appointments;
    this.encounters = encounters;
    this.patients = patients;
    this.vitals = vitals;
    this.consultations = consultations;
    this.care = care;
    this.db = db;
  }

  @Transactional
  public Encounter checkIn(Long id) {
    Appointment a =
        appointments
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    if ("CANCELLED".equalsIgnoreCase(a.getStatus()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled appointment");
    if (!patients.existsById(a.getPatientId()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient not found");
    return encounters
        .findByAppointmentId(id)
        .orElseGet(
            () -> {
              Encounter e = new Encounter();
              e.setEncounterNo(
                  "ENC-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
              e.setDoctorEmployeeId(a.getDoctorEmployeeId());
              e.setAppointmentId(id);
              e.setPatientId(a.getPatientId());
              e.setDepartmentId(a.getDepartmentId());
              e.setDoctorName(a.getDoctorName());
              e.setEncounterType("OPD");
              e.setStatus("CHECKED_IN");
              e.setChiefComplaint(a.getReason());
              e.setStartedAt(LocalDateTime.now());
              return encounters.save(e);
            });
  }

  @Transactional
  public Encounter walkIn(ClinicalWorkflowWalkInRequest request) {
    if (request.patientId() == null || !patients.existsById(request.patientId()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid patientId required");
    Encounter e = new Encounter();
    e.setEncounterNo("ENC-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
    if (care != null) {
      long doctor =
          request.doctorEmployeeId() == null
              ? care.doctorId(request.doctorName())
              : request.doctorEmployeeId();
      e.setDoctorEmployeeId(doctor);
      e.setDoctorName(care.doctorName(doctor));
      e.setDepartmentId(care.departmentId(doctor));
    } else e.setDoctorName(request.doctorName());
    e.setPatientId(request.patientId());
    e.setEncounterType("OPD");
    e.setStatus("CHECKED_IN");
    e.setChiefComplaint(request.chiefComplaint());
    e.setStartedAt(LocalDateTime.now());
    return encounters.save(e);
  }

  @Transactional
  public Vital recordVitals(Long id, Vital vital) {
    Encounter e =
        encounters
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encounter not found"));
    if ("COMPLETED".equals(e.getStatus()) || "CANCELLED".equals(e.getStatus()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Encounter is closed");
    if ((vital.getSpo2() != null
            && (vital.getSpo2().signum() < 0
                || vital.getSpo2().compareTo(new java.math.BigDecimal("100")) > 0))
        || (vital.getPulse() != null && vital.getPulse() <= 0)
        || (vital.getSystolic() != null && vital.getSystolic() <= 0)
        || (vital.getDiastolic() != null && vital.getDiastolic() <= 0)
        || (vital.getWeightKg() != null && vital.getWeightKg().signum() <= 0)
        || (vital.getHeightCm() != null && vital.getHeightCm().signum() <= 0))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Vitals must use positive values; SpO2 must be between zero and 100");
    vital.setId(null);
    vital.setEncounterId(id);
    vital.setRecordedAt(LocalDateTime.now());
    Vital saved = vitals.save(vital);
    e.setStatus("WAITING");
    encounters.save(e);
    return saved;
  }

  @Transactional
  public Encounter startConsultation(Long id) {
    Encounter e =
        encounters
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encounter not found"));
    if (!Set.of("CHECKED_IN", "WAITING").contains(e.getStatus()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Encounter cannot start consultation");
    if (db != null) db.updateEncountersForStartConsultation(id);
    e.setStatus("IN_CONSULTATION");
    return encounters.save(e);
  }

  @Transactional
  public Consultation saveConsultation(Long id, Consultation consultation) {
    Encounter e =
        encounters
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encounter not found"));
    if (!"IN_CONSULTATION".equals(e.getStatus()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Start consultation first");
    if (consultation.getDiagnosis() == null || consultation.getDiagnosis().isBlank())
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Diagnosis required before saving consultation");
    consultation.setId(null);
    consultation.setEncounterId(id);
    consultation.setCreatedAt(LocalDateTime.now());
    return consultations.save(consultation);
  }

  @Transactional
  public Encounter complete(Long id) {
    Encounter e =
        encounters
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encounter not found"));
    if (!"IN_CONSULTATION".equals(e.getStatus()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Encounter is not in consultation");
    if (db != null) {
      if (db.countConsultationsForComplete(id) == 0)
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Save consultation notes before completion");
      if (db.countInvoicesForComplete(id) > 0)
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Outstanding visit billing must be settled before completion");
      db.updateEncountersForComplete(
          org.springframework.security.core.context.SecurityContextHolder.getContext()
              .getAuthentication()
              .getName(),
          id);
    }
    e.setStatus("COMPLETED");
    e.setCompletedAt(LocalDateTime.now());
    encounters.save(e);
    if (e.getAppointmentId() != null)
      appointments
          .findById(e.getAppointmentId())
          .ifPresent(
              a -> {
                a.setStatus("COMPLETED");
                appointments.save(a);
              });
    return e;
  }
}
