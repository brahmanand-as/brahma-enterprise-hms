package com.brahma.arogya.portal.service;

import com.brahma.arogya.portal.dto.PersonalWorkspacePasswordChangeRequest;
import com.brahma.arogya.portal.repository.PersonalWorkspaceJdbcRepository;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class PersonalWorkspaceService {

  private final PersonalWorkspaceJdbcRepository db;

  private final PasswordEncoder encoder;

  public PersonalWorkspaceService(PersonalWorkspaceJdbcRepository db, PasswordEncoder encoder) {
    this.db = db;
    this.encoder = encoder;
  }

  public List<Map<String, Object>> notifications(Principal p) {
    return db.findNotificationsForNotifications(p.getName());
  }

  public void read(long id, Principal p) {
    db.updateNotificationsForRead(id, p.getName());
  }

  public void password(PersonalWorkspacePasswordChangeRequest f, Principal p) {
    String hash = db.readStaffAccountsForPassword(p.getName());
    if (f.newPassword() == null
        || f.newPassword().length() < 12
        || !encoder.matches(f.currentPassword(), hash))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Current password and new password of 12+ characters required");
    db.updateStaffAccountsForPassword(encoder.encode(f.newPassword()), p.getName());
  }

  public void logout(Principal p) {
    db.updateStaffAccountsForLogout(p.getName());
  }
}
