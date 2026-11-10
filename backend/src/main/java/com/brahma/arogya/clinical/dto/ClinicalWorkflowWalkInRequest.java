package com.brahma.arogya.clinical.dto;

import java.util.*;

public record ClinicalWorkflowWalkInRequest(
    @jakarta.validation.constraints.Positive() Long patientId,
    String doctorName,
    String chiefComplaint,
    @jakarta.validation.constraints.Positive() Long doctorEmployeeId) {

  public ClinicalWorkflowWalkInRequest(Long patient, String doctor, String complaint) {
    this(patient, doctor, complaint, null);
  }
}
