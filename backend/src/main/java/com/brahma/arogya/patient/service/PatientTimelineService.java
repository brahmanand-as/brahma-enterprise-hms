package com.brahma.arogya.patient.service;

import com.brahma.arogya.patient.repository.PatientTimelineJdbcRepository;
import com.brahma.arogya.security.service.CareScope;
import java.util.*;

@org.springframework.stereotype.Service()
public class PatientTimelineService {

  private final PatientTimelineJdbcRepository db;

  private final CareScope care;

  public PatientTimelineService(PatientTimelineJdbcRepository db, CareScope care) {
    this.db = db;
    this.care = care;
  }

  public Map<String, Object> timeline(long id) {
    care.require(id);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("patient", db.findPatientsForTimeline(id));
    out.put("visits", db.findEncountersForTimeline(id));
    out.put("consultations", db.findConsultationsForTimeline(id));
    out.put("vitals", db.findVitalsForTimeline(id));
    out.put("documents", db.findIssuedDocumentsForTimeline(id));
    return out;
  }
}
