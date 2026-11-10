package com.brahma.arogya.laboratory.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class LabDispatchJdbcRepository {

  private final JdbcTemplate jdbc;

  public LabDispatchJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findSampleDispatchesForAll() {
    return jdbc.queryForList("SELECT * FROM sample_dispatches ORDER BY id DESC");
  }

  public Map<String, Object> findLabOrderItemsForDispatch(Long labOrderItemId) {
    return jdbc.queryForMap("SELECT * FROM lab_order_items WHERE id=? FOR UPDATE", labOrderItemId);
  }

  public Integer countSampleDispatchesForDispatch(Long labOrderItemId) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM sample_dispatches WHERE lab_order_item_id=?",
        Integer.class,
        labOrderItemId);
  }

  public int insertSampleDispatchesForDispatch(
      Long labOrderItemId,
      Object externalLabId,
      String sampleId,
      String courierDetails,
      String trackingNumber) {
    return jdbc.update(
        "INSERT INTO"
            + " sample_dispatches(lab_order_item_id,external_lab_id,sample_id,courier_details,tracking_number,dispatch_time,status)"
            + " VALUES(?,?,?,?,?,NOW(),'DISPATCHED')",
        labOrderItemId,
        externalLabId,
        sampleId,
        courierDetails,
        trackingNumber);
  }

  public int updateSampleDispatchesForReceive(long id) {
    return jdbc.update(
        "UPDATE sample_dispatches SET status='RECEIVED',received_time=NOW() WHERE id=? AND"
            + " status='DISPATCHED'",
        id);
  }
}
