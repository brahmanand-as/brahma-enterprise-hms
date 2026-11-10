package com.brahma.arogya.hr.service;

import com.brahma.arogya.hr.entity.Employee;
import com.brahma.arogya.hr.repository.EmployeeJdbcRepository;
import com.brahma.arogya.hr.repository.EmployeeRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class EmployeeService {

  private final EmployeeRepository r;

  private final EmployeeJdbcRepository db;

  public EmployeeService(EmployeeRepository r, EmployeeJdbcRepository db) {
    this.r = r;
    this.db = db;
  }

  public List<Employee> all() {
    return r.findAll();
  }

  public Employee one(Long id) {
    return r.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
  }

  @Transactional
  public Employee create(Employee x) {
    if (x.getId() != null)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "ID must be assigned by the database");
    if (x.getName() == null || x.getName().isBlank())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee name is required");
    Long next = db.readEmployeeNumberSequenceForCreate();
    if (next == null)
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Employee sequence missing");
    db.updateEmployeeNumberSequenceForCreate();
    x.setEmployeeNo(String.format("EMP-%06d", next));
    if (x.getStatus() == null || x.getStatus().isBlank()) x.setStatus("ACTIVE");
    return r.saveAndFlush(x);
  }

  @Transactional
  public Employee update(Long id, Employee x) {
    Employee e = one(id);
    e.setName(x.getName());
    e.setDepartment(x.getDepartment());
    e.setDesignation(x.getDesignation());
    e.setMobile(x.getMobile());
    if (x.getStatus() != null) e.setStatus(x.getStatus());
    if ("INACTIVE".equals(e.getStatus())) {
      db.updateStaffAccountsForUpdate(id);
      db.updateAppointmentSlotsForUpdate(id);
      db.insertRescheduleRequestsForUpdate(id);
    }
    return r.save(e);
  }

  @Transactional
  public void delete(Long id) {
    one(id);
    Integer linked = db.countStaffAccountsForDelete(id);
    Integer leaves = db.countLeaveRequestsForDelete(id);
    Integer salaries = db.countSalarySlipsForDelete(id);
    Integer profiles = db.countDoctorProfilesForDelete(id);
    if ((profiles != null && profiles > 0)
        || (linked != null && linked > 0)
        || (leaves != null && leaves > 0)
        || (salaries != null && salaries > 0))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Employee has linked accounts or HR history. Set status to INACTIVE instead.");
    r.deleteById(id);
  }
}
