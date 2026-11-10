package com.brahma.arogya.security.service;

import com.brahma.arogya.security.dto.SecurityAdministrationAccountChangeRequest;
import com.brahma.arogya.security.dto.SecurityAdministrationRoleFormRequest;
import com.brahma.arogya.security.dto.SecurityAdministrationSettingRequest;
import com.brahma.arogya.security.repository.SecurityAdministrationJdbcRepository;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class SecurityAdministrationService {

  private final SecurityAdministrationJdbcRepository db;

  private final PasswordEncoder passwords;

  public SecurityAdministrationService(
      SecurityAdministrationJdbcRepository db, PasswordEncoder passwords) {
    this.db = db;
    this.passwords = passwords;
  }

  public List<Map<String, Object>> permissions() {
    return db.findPermissionsForPermissions();
  }

  public List<Map<String, Object>> roles() {
    var rows = db.findEnterpriseRolesForRoles();
    for (var r : rows) r.put("permissions", db.findRolePermissionsForRoles(r.get("name")));
    return rows;
  }

  @Transactional
  public Map<String, String> role(SecurityAdministrationRoleFormRequest f) {
    if (f.name() == null
        || !f.name().matches("[A-Z][A-Z0-9_]{2,39}")
        || f.label() == null
        || f.label().isBlank()
        || f.permissions() == null) throw bad("Role name, label and permissions required");
    db.insertEnterpriseRolesForRole(f.name(), f.label());
    savePermissions(f.name(), f.permissions());
    return Map.of("name", f.name());
  }

  @Transactional
  public Map<String, String> update(String name, SecurityAdministrationRoleFormRequest f) {
    var role = db.findEnterpriseRolesForUpdate(name);
    if (Boolean.TRUE.equals(role.get("system_role")))
      throw conflict("System role safeguards cannot be changed; create a custom role");
    if (f.permissions() == null) throw bad("Permission selection required");
    db.deleteRolePermissionsForUpdate(name);
    savePermissions(name, f.permissions());
    return Map.of("status", "UPDATED");
  }

  private void savePermissions(String name, List<String> permissions) {
    for (String code : new HashSet<>(permissions)) {
      if (db.countPermissionsForSavePermissions(code) == 0) throw bad("Unknown permission");
      if (code.equals("security.manage"))
        throw bad("Security administration is reserved for the protected administrator role");
      db.insertRolePermissionsForSavePermissions(name, code);
    }
  }

  public List<Map<String, Object>> accounts() {
    return db.findEmployeesForAccounts();
  }

  @Transactional
  public Map<String, String> change(
      long id, SecurityAdministrationAccountChangeRequest f, Principal p) {
    var a = db.findStaffAccountsForChange(id);
    if (a.get("username").equals(p.getName()))
      throw conflict(
          "Use your profile to change your password; self-disable and self-role changes are"
              + " blocked");
    if ("ADMIN".equals(a.get("role"))) {
      db.findStaffAccountsForChange2();
      long count = db.countStaffAccountsForChange();
      if (count <= 1
          && (Boolean.FALSE.equals(f.enabled()) || (f.role() != null && !f.role().equals("ADMIN"))))
        throw conflict("Cannot remove the last enabled administrator");
    }
    if (f.role() != null) {
      if (db.countEnterpriseRolesForChange(f.role()) == 0) throw bad("Unknown role");
      db.updateStaffAccountsForChange(f.role(), id);
    }
    if (f.enabled() != null) db.updateStaffAccountsForChange2(f.enabled(), id);
    if (f.password() != null) {
      if (f.password().length() < 12) throw bad("Password requires 12 characters");
      db.updateStaffAccountsForChange3(passwords.encode(f.password()), id);
    }
    db.updateStaffAccountsForChange4(id);
    return Map.of("status", "UPDATED");
  }

  public List<Map<String, Object>> audit(int limit) {
    return db.findAuditLogsForAudit(Math.max(1, Math.min(limit, 1000)));
  }

  public List<Map<String, Object>> settings() {
    return db.findHospitalSettingsForSettings();
  }

  public Map<String, String> setting(String key, SecurityAdministrationSettingRequest f) {
    if (f.value() == null || f.value().length() > 500) throw bad("Invalid setting");
    if (key.equals("registration_fee") || key.equals("reschedule_cutoff_minutes")) {
      try {
        if (new java.math.BigDecimal(f.value()).signum() < 0) throw new Exception();
      } catch (Exception e) {
        throw bad("Nonnegative numeric setting required");
      }
    }
    if (db.updateHospitalSettingsForSetting(f.value(), key) == 0) throw bad("Unknown setting");
    return Map.of("status", "UPDATED");
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  private ResponseStatusException conflict(String s) {
    return new ResponseStatusException(HttpStatus.CONFLICT, s);
  }
}
