package com.brahma.arogya.clinical.controller;

import com.brahma.arogya.clinical.dto.ClinicalWorkflowWalkInRequest;
import com.brahma.arogya.clinical.dto.ConsultationRequest;
import com.brahma.arogya.clinical.dto.ConsultationResponse;
import com.brahma.arogya.clinical.dto.EncounterResponse;
import com.brahma.arogya.clinical.dto.VitalRequest;
import com.brahma.arogya.clinical.dto.VitalResponse;
import com.brahma.arogya.clinical.mapper.ConsultationMapper;
import com.brahma.arogya.clinical.mapper.EncounterMapper;
import com.brahma.arogya.clinical.mapper.VitalMapper;
import com.brahma.arogya.clinical.service.ClinicalWorkflowService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
public class ClinicalWorkflowController {

  private final ClinicalWorkflowService service;

  public ClinicalWorkflowController(ClinicalWorkflowService service) {
    this.service = service;
  }

  @PostMapping("/appointments/{id}/check-in")
  public EncounterResponse checkIn(@PathVariable Long id) {
    return EncounterMapper.toResponse(service.checkIn(id));
  }

  @PostMapping("/walk-in")
  public EncounterResponse walkIn(
      @RequestBody @jakarta.validation.Valid() ClinicalWorkflowWalkInRequest request) {
    return EncounterMapper.toResponse(service.walkIn(request));
  }

  @PostMapping("/encounters/{id}/vitals")
  public VitalResponse recordVitals(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() VitalRequest vital) {
    return VitalMapper.toResponse(service.recordVitals(id, VitalMapper.toEntity(vital)));
  }

  @PostMapping("/encounters/{id}/start-consultation")
  public EncounterResponse startConsultation(@PathVariable Long id) {
    return EncounterMapper.toResponse(service.startConsultation(id));
  }

  @PostMapping("/encounters/{id}/consultation")
  public ConsultationResponse saveConsultation(
      @PathVariable Long id,
      @RequestBody @jakarta.validation.Valid() ConsultationRequest consultation) {
    return ConsultationMapper.toResponse(
        service.saveConsultation(id, ConsultationMapper.toEntity(consultation)));
  }

  @PostMapping("/encounters/{id}/complete")
  public EncounterResponse complete(@PathVariable Long id) {
    return EncounterMapper.toResponse(service.complete(id));
  }
}
