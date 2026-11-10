package com.brahma.arogya.portal.service;

import com.brahma.arogya.portal.repository.PortalDataJdbcRepository;
import com.brahma.arogya.security.service.AccessPolicy;
import com.brahma.arogya.security.service.CareScope;
import java.util.*;
import org.springframework.security.core.Authentication;

@org.springframework.stereotype.Service()
public class PortalDataService {

  private final PortalDataJdbcRepository db;

  private final AccessPolicy access;

  private final CareScope care;

  public PortalDataService(PortalDataJdbcRepository db, AccessPolicy access, CareScope care) {
    this.db = db;
    this.access = access;
    this.care = care;
  }

  public Map<String, Object> options(Authentication a) {
    Map<String, Object> o = new LinkedHashMap<>();
    boolean clinical = access.has(a, "clinical.read"), ipd = access.has(a, "ipd.read");
    if (access.has(a, "patients.read")) o.put("patients", filter(db.findPatientsForOptions()));
    if (clinical || access.has(a, "billing.read"))
      o.put("encounters", filter(db.findEncountersForOptions()));
    if (ipd || access.has(a, "pharmacy.read") || access.has(a, "insurance.manage"))
      o.put("admissions", filter(db.findAdmissionsForOptions()));
    if (access.has(a, "staff.manage")
        || access.has(a, "roster.manage")
        || access.has(a, "roster.read")) o.put("employees", db.findEmployeesForOptions());
    if (access.has(a, "clinical.checkin") || access.has(a, "appointments.manage"))
      o.put("clinicalStaff", db.findStaffAccountsForOptions());
    if (access.has(a, "patients.write") || clinical)
      o.put("doctors", db.findEmployeesForOptions2());
    if (ipd) o.put("beds", db.findIpdBedsForOptions());
    if (access.has(a, "pharmacy.read") || access.has(a, "prescription.write"))
      o.put("inventory", db.findInventoryItemsForOptions());
    if (access.has(a, "lab.read")) o.put("labTests", db.findLabTestCatalogForOptions2());
    if (access.has(a, "lab.read")) o.put("externalLabs", db.findExternalLabsForOptions());
    if (access.has(a, "billing.read")) o.put("invoices", db.findInvoicesForOptions());
    if (access.has(a, "helpdesk.manage")) o.put("helpdeskStaff", db.findStaffAccountsForOptions2());
    if (access.has(a, "staff.manage")) o.put("roles", db.findEnterpriseRolesForOptions());
    o.put("departments", db.findDepartmentsForOptions());
    return o;
  }

  private List<Map<String, Object>> filter(List<Map<String, Object>> list) {
    return list.stream().filter(care::visible).toList();
  }
}
