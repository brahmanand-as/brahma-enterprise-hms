package com.brahma.arogya.security.service;

import com.brahma.arogya.appointment.entity.Appointment;
import com.brahma.arogya.clinical.entity.Consultation;
import com.brahma.arogya.clinical.entity.Encounter;
import com.brahma.arogya.clinical.entity.Vital;
import com.brahma.arogya.inpatient.entity.Admission;
import com.brahma.arogya.laboratory.entity.LabOrder;
import com.brahma.arogya.patient.entity.Patient;
import com.brahma.arogya.radiology.entity.RadiologyOrder;
import com.brahma.arogya.security.repository.CareScopeJdbcRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CareScope {

  private final CareScopeJdbcRepository db;

  private final AccessPolicy access;

  public CareScope(CareScopeJdbcRepository db, AccessPolicy access) {
    this.db = db;
    this.access = access;
  }

  public boolean restricted() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    return a != null
        && (access.role(a, "DOCTOR")
            || access.role(a, "NURSE")
            || access.has(a, "clinical.consult")
            || access.has(a, "nursing.write"))
        && !access.role(a, "ADMIN");
  }

  public boolean can(long patient) {
    var a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null) return false;
    if (!restricted()) return true;
    String u = a.getName();
    return db.countCareTeamsForCan(patient, u, patient, u, patient, u) > 0;
  }

  public void require(long patient) {
    if (!can(patient))
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Record is outside your assigned care team");
  }

  public String doctorName(long id) {
    return db.readEmployeesForDoctorName(id);
  }

  public Long departmentId(long employee) {
    var ids = db.findDepartmentsForDepartmentId(employee);
    if (ids.isEmpty())
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Configure the doctor's hospital department before booking");
    return ids.get(0);
  }

  public long doctorId(String name) {
    var ids = db.findEmployeesForDoctorId(name);
    if (ids.size() != 1)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select one active linked doctor");
    return ids.get(0);
  }

  public void encounter(long id) {
    if (restricted()) require(db.readEncountersForEncounter(id));
  }

  public void admission(long id) {
    if (restricted()) require(db.readAdmissionsForAdmission(id));
  }

  public boolean visible(Object o) {
    if (!restricted()) return true;
    if (o instanceof com.brahma.arogya.common.scope.PatientScopedResponse scoped) {
      Long patientId = scoped.scopePatientId();
      return patientId == null || can(patientId);
    }
    if (o instanceof com.brahma.arogya.common.scope.EncounterScopedResponse scoped) {
      try {
        return can(db.readEncountersForVisible(scoped.scopeEncounterId()));
      } catch (Exception ignored) {
        return false;
      }
    }
    Long patient = null;
    if (o instanceof Patient p) patient = p.getId();
    else if (o instanceof Appointment a) patient = a.getPatientId();
    else if (o instanceof Encounter e) patient = e.getPatientId();
    else if (o instanceof Admission a) patient = a.getPatientId();
    else if (o instanceof LabOrder l) patient = l.getPatientId();
    else if (o instanceof RadiologyOrder r) patient = r.getPatientId();
    else if (o instanceof Vital v) {
      try {
        patient = db.readEncountersForVisible(v.getEncounterId());
      } catch (Exception ignored) {
        return false;
      }
    } else if (o instanceof Consultation c) {
      try {
        patient = db.readEncountersForVisible2(c.getEncounterId());
      } catch (Exception ignored) {
        return false;
      }
    } else if (o instanceof Map<?, ?> m) {
      Object p = m.containsKey("patient_id") ? m.get("patient_id") : m.get("patientId");
      if (p instanceof Number n) patient = n.longValue();
      else if (m.get("admission_id") instanceof Number n) {
        try {
          patient = db.readAdmissionsForVisible(n.longValue());
        } catch (Exception ignored) {
          return false;
        }
      }
    }
    return patient == null || can(patient);
  }
}
