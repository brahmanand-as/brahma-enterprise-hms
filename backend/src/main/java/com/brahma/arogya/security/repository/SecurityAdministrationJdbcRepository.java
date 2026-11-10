package com.brahma.arogya.security.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class SecurityAdministrationJdbcRepository {

  private final JdbcTemplate jdbc;

  public SecurityAdministrationJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findPermissionsForPermissions() {
    return jdbc.queryForList("SELECT * FROM permissions ORDER BY code");
  }

  public List<Map<String, Object>> findEnterpriseRolesForRoles() {
    return jdbc.queryForList("SELECT * FROM enterprise_roles ORDER BY name");
  }

  public List<String> findRolePermissionsForRoles(Object name) {
    return jdbc.queryForList(
        "SELECT permission_code FROM role_permissions WHERE role_name=? ORDER BY"
            + " permission_code",
        String.class,
        name);
  }

  public int insertEnterpriseRolesForRole(String name, String label) {
    return jdbc.update("INSERT INTO enterprise_roles(name,label) VALUES(?,?)", name, label);
  }

  public Map<String, Object> findEnterpriseRolesForUpdate(String name) {
    return jdbc.queryForMap("SELECT * FROM enterprise_roles WHERE name=? FOR UPDATE", name);
  }

  public int deleteRolePermissionsForUpdate(String name) {
    return jdbc.update("DELETE FROM role_permissions WHERE role_name=?", name);
  }

  public Integer countPermissionsForSavePermissions(String code) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM permissions WHERE code=?", Integer.class, code);
  }

  public int insertRolePermissionsForSavePermissions(String name, String code) {
    return jdbc.update("INSERT INTO role_permissions VALUES(?,?)", name, code);
  }

  public List<Map<String, Object>> findEmployeesForAccounts() {
    return jdbc.queryForList(
        "SELECT a.id,a.username,a.role,a.enabled,a.employee_id,e.name employee_name FROM"
            + " staff_accounts a LEFT JOIN employees e ON e.id=a.employee_id ORDER BY a.username");
  }

  public Map<String, Object> findStaffAccountsForChange(long id) {
    return jdbc.queryForMap("SELECT * FROM staff_accounts WHERE id=? FOR UPDATE", id);
  }

  public List<Map<String, Object>> findStaffAccountsForChange2() {
    return jdbc.queryForList("SELECT id FROM staff_accounts WHERE role='ADMIN' FOR UPDATE");
  }

  public Long countStaffAccountsForChange() {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM staff_accounts WHERE role='ADMIN' AND enabled=1", Long.class);
  }

  public Integer countEnterpriseRolesForChange(String role) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM enterprise_roles WHERE name=?", Integer.class, role);
  }

  public int updateStaffAccountsForChange(String role, long id) {
    return jdbc.update("UPDATE staff_accounts SET role=? WHERE id=?", role, id);
  }

  public int updateStaffAccountsForChange2(Boolean enabled, long id) {
    return jdbc.update("UPDATE staff_accounts SET enabled=? WHERE id=?", enabled, id);
  }

  public int updateStaffAccountsForChange3(Object value1, long id) {
    return jdbc.update("UPDATE staff_accounts SET password_hash=? WHERE id=?", value1, id);
  }

  public int updateStaffAccountsForChange4(long id) {
    return jdbc.update("UPDATE staff_accounts SET token_version=token_version+1 WHERE id=?", id);
  }

  public List<Map<String, Object>> findAuditLogsForAudit(int value1) {
    return jdbc.queryForList("SELECT * FROM audit_logs ORDER BY id DESC LIMIT ?", value1);
  }

  public List<Map<String, Object>> findHospitalSettingsForSettings() {
    return jdbc.queryForList("SELECT * FROM hospital_settings ORDER BY setting_key");
  }

  public int updateHospitalSettingsForSetting(String value, String key) {
    return jdbc.update(
        "UPDATE hospital_settings SET setting_value=? WHERE setting_key=?", value, key);
  }
}
