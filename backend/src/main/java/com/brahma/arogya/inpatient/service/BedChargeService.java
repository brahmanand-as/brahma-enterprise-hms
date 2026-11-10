package com.brahma.arogya.inpatient.service;

import com.brahma.arogya.billing.service.RevenueLedger;
import com.brahma.arogya.inpatient.repository.BedChargeJdbcRepository;
import java.math.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BedChargeService {

  private final BedChargeJdbcRepository db;

  private final RevenueLedger ledger;

  public BedChargeService(BedChargeJdbcRepository db, RevenueLedger ledger) {
    this.db = db;
    this.ledger = ledger;
  }

  @Transactional
  public void open(long admission, long bed) {
    db.insertAdmissionBedSegmentsForOpen(admission, bed);
  }

  @Transactional
  public void close(long admission) {
    var segments = db.findAdmissionBedSegmentsForClose(admission);
    for (var segment : segments) {
      Object raw = segment.get("started_at");
      LocalDateTime start =
          raw instanceof LocalDateTime t ? t : ((java.sql.Timestamp) raw).toLocalDateTime();
      long minutes = Math.max(1, Duration.between(start, LocalDateTime.now()).toMinutes());
      BigDecimal charge =
          ((BigDecimal) segment.get("daily_rate"))
              .multiply(BigDecimal.valueOf(minutes))
              .divide(BigDecimal.valueOf(1440), 2, RoundingMode.HALF_UP);
      var a = db.findAdmissionsForClose(admission);
      long invoice =
          ledger.charge(
              ((Number) a.get("patient_id")).longValue(),
              a.get("encounter_id") == null ? null : ((Number) a.get("encounter_id")).longValue(),
              "BED-STAY-" + segment.get("id"),
              "BED_STAY",
              ((Number) segment.get("id")).longValue(),
              "Ward/ICU bed stay, " + minutes + " minutes (daily rate prorated)",
              BigDecimal.ONE,
              charge);
      db.updateAdmissionBedSegmentsForClose(invoice, segment.get("id"));
    }
  }
}
