package com.brahma.arogya.hr.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class StaffPortalJdbcRepository {

  private final JdbcTemplate jdbc;

  public StaffPortalJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Integer countEnterpriseRolesForCreate(String role) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM enterprise_roles WHERE name=?", Integer.class, role);
  }

  public Integer countEmployeesForCreate(Long employeeId) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM employees WHERE id=?", Integer.class, employeeId);
  }

  public int insertStaffAccountsForCreate(
      String username, Object value2, String role, Long employeeId) {
    return jdbc.update(
        "INSERT INTO staff_accounts(username,password_hash,role,employee_id,enabled) VALUES"
            + " (?,?,?,?,1)",
        username,
        value2,
        role,
        employeeId);
  }

  public List<Map<String, Object>> findStaffAccountsForStaff() {
    return jdbc.queryForList(
        "SELECT id,username,role,employee_id,enabled FROM staff_accounts ORDER BY id");
  }

  public List<Long> findStaffAccountsForEmployeeId(String name) {
    return jdbc.query(
        "SELECT employee_id FROM staff_accounts WHERE username=?",
        (rs, n) -> rs.getObject(1, Long.class),
        name);
  }

  public List<Map<String, Object>> findEmployeesForProfile(long value1) {
    return jdbc.queryForList(
        "SELECT id,employee_no,name,department,designation,mobile,status FROM employees WHERE"
            + " id=?",
        value1);
  }

  public List<Map<String, Object>> findSalarySlipsForSalary(long value1) {
    return jdbc.queryForList(
        "SELECT id,salary_month,basic,allowances,deductions,net_pay,payment_status,credited_at FROM"
            + " salary_slips WHERE employee_id=? ORDER BY salary_month DESC",
        value1);
  }

  public List<Map<String, Object>> findLeaveRequestsForLeaves(long value1) {
    return jdbc.queryForList(
        "SELECT * FROM leave_requests WHERE employee_id=? ORDER BY requested_at DESC", value1);
  }

  public int insertLeaveRequestsForApply(
      long value1, String leaveType, LocalDate start, LocalDate end, String reason) {
    return jdbc.update(
        "INSERT INTO leave_requests(employee_id,leave_type,start_date,end_date,reason,status)"
            + " VALUES(?,?,?,?,?,'PENDING')",
        value1,
        leaveType,
        start,
        end,
        reason);
  }

  public List<String> findDepartmentHeadsForApply(long value2) {
    return jdbc.queryForList(
        "SELECT h.username FROM department_heads h JOIN employees e ON"
            + " e.department=h.department WHERE e.id=?",
        String.class,
        value2);
  }

  public List<Map<String, Object>> findLeaveRequestsForAllLeaves() {
    return jdbc.queryForList(
        "SELECT l.*,e.name employee_name FROM leave_requests l JOIN employees e ON"
            + " e.id=l.employee_id ORDER BY l.requested_at DESC");
  }

  public List<Map<String, Object>> findLeaveRequestsForAllLeaves2(String name) {
    return jdbc.queryForList(
        "SELECT l.*,e.name employee_name FROM leave_requests l JOIN employees e ON"
            + " e.id=l.employee_id JOIN department_heads h ON h.department=e.department WHERE"
            + " h.username=? ORDER BY l.requested_at DESC",
        name);
  }

  public Long readLeaveRequestsForReview(Long id) {
    return jdbc.queryForObject("SELECT employee_id FROM leave_requests WHERE id=?", Long.class, id);
  }

  public int updateLeaveRequestsForReview(String status, String name, Long id) {
    return jdbc.update(
        "UPDATE leave_requests SET status=?,reviewed_by=?,reviewed_at=NOW() WHERE id=? AND"
            + " status='PENDING'",
        status,
        name,
        id);
  }

  public Map<String, Object> findLeaveRequestsForReview(Long id) {
    return jdbc.queryForMap("SELECT * FROM leave_requests WHERE id=?", id);
  }

  public int insertSalarySlipsForSalaryEntry(
      Long employeeId,
      String salaryMonth,
      BigDecimal basic,
      BigDecimal allowances,
      BigDecimal deductions,
      BigDecimal net,
      String status) {
    return jdbc.update(
        "INSERT INTO"
            + " salary_slips(employee_id,salary_month,basic,allowances,deductions,net_pay,payment_status)"
            + " VALUES(?,?,?,?,?,?,?)",
        employeeId,
        salaryMonth,
        basic,
        allowances,
        deductions,
        net,
        status);
  }
}
