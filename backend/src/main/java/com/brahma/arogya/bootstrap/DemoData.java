package com.brahma.arogya.bootstrap;

import com.brahma.arogya.bootstrap.repository.DemoDataJdbcRepository;
import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
public class DemoData implements CommandLineRunner {

  private final DemoDataJdbcRepository db;

  private final PasswordEncoder encoder;

  private final String password;

  public DemoData(
      DemoDataJdbcRepository db,
      PasswordEncoder encoder,
      @Value("${HMS_DEMO_PASSWORD:}") String password) {
    this.db = db;
    this.encoder = encoder;
    this.password = password;
  }

  @Transactional
  public void run(String... args) {
    if (password.length() < 12)
      throw new IllegalStateException("Demo profile requires HMS_DEMO_PASSWORD of 12+ characters");
    if (db.countPatientsForRun() > 0) return;
    String demoHash = encoder.encode(password);
    db.updateIpdBedsForRun();
    db.insertPatientsForRun();
    long pid = db.readGeneratedIdForRun();
    db.updatePatientNumberSequenceForRun();
    db.insertStaffAccountsForRun(demoHash, pid);
    String[] roles = {
      "DOCTOR",
      "NURSE",
      "RECEPTIONIST",
      "IPD",
      "LAB_TECHNICIAN",
      "LAB_SUPERVISOR",
      "PATHOLOGIST",
      "RADIOLOGIST",
      "PHARMACIST",
      "BILLING",
      "INSURANCE",
      "HR",
      "HELPDESK"
    };
    int seq = 1;
    for (String role : roles) {
      String name = role.equals("DOCTOR") ? "Dr Synthetic Demo" : "Synthetic " + role;
      db.insertEmployeesForRun(String.format("EMP-%06d", seq++), name, role);
      long eid = db.readGeneratedIdForRun2();
      db.insertStaffAccountsForRun2("demo." + role.toLowerCase(), demoHash, role, eid);
    }
    db.updateEmployeeNumberSequenceForRun(seq);
    for (int d = 1; d <= 7; d++)
      for (int h = 9; h < 13; h++) {
        LocalDateTime t = LocalDate.now().plusDays(d).atTime(h, 0);
        db.insertAppointmentSlotsForRun(t, t.plusMinutes(30));
      }
    db.insertInventoryItemsForRun(LocalDate.now().plusYears(1));
    for (int d = 1; d <= 7; d++)
      db.insertDutyRosterForRun(LocalDate.now().plusDays(d), LocalDate.now().plusDays(d));
    db.updateAppointmentSlotsForRun();
    db.insertCareTeamsForRun(pid);
    db.insertInventoryBatchesForRun2();
    db.insertExternalLabsForRun();
  }
}
