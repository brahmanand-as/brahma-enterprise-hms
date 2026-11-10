package com.brahma.arogya.laboratory.service;

import com.brahma.arogya.laboratory.dto.LabDispatchDispatchRequest;
import com.brahma.arogya.laboratory.repository.LabDispatchJdbcRepository;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class LabDispatchService {

  private final LabDispatchJdbcRepository db;

  public LabDispatchService(LabDispatchJdbcRepository db) {
    this.db = db;
  }

  public List<Map<String, Object>> all() {
    return db.findSampleDispatchesForAll();
  }

  @Transactional
  public Map<String, String> dispatch(LabDispatchDispatchRequest r, Principal p) {
    if (r.labOrderItemId() == null
        || r.sampleId() == null
        || r.sampleId().isBlank()
        || r.courierDetails() == null
        || r.trackingNumber() == null)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Item, sample and courier tracking required");
    var item = db.findLabOrderItemsForDispatch(r.labOrderItemId());
    if (!"OUTSOURCE".equals(item.get("processing_type"))
        || !"SAMPLE_COLLECTED".equals(item.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Collect an outsourced sample first");
    if (db.countSampleDispatchesForDispatch(r.labOrderItemId()) > 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Sample already dispatched");
    db.insertSampleDispatchesForDispatch(
        r.labOrderItemId(),
        item.get("external_lab_id"),
        r.sampleId(),
        r.courierDetails(),
        r.trackingNumber());
    return Map.of("status", "DISPATCHED");
  }

  public Map<String, String> receive(long id) {
    if (db.updateSampleDispatchesForReceive(id) == 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Dispatch not awaiting receipt");
    return Map.of("status", "RECEIVED");
  }
}
