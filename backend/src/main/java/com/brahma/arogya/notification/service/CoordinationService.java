package com.brahma.arogya.notification.service;

import com.brahma.arogya.notification.repository.CoordinationJdbcRepository;
import org.springframework.stereotype.Service;

@Service
public class CoordinationService {

  private final CoordinationJdbcRepository db;

  public CoordinationService(CoordinationJdbcRepository db) {
    this.db = db;
  }

  public void notify(String username, String subject, String message) {
    db.insertNotificationsForNotify(username, subject, message);
  }

  public void patient(long id, String subject, String message) {
    for (String u : db.findStaffAccountsForPatient(id)) notify(u, subject, message);
  }

  public void department(String permission, String subject, String message) {
    for (String u : db.findStaffAccountsForDepartment(permission)) notify(u, subject, message);
  }
}
