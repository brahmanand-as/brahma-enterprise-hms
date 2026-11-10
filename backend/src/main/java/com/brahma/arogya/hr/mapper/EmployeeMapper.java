package com.brahma.arogya.hr.mapper;

import com.brahma.arogya.hr.dto.EmployeeRequest;
import com.brahma.arogya.hr.dto.EmployeeResponse;
import com.brahma.arogya.hr.entity.Employee;

public final class EmployeeMapper {

  public static Employee toEntity(EmployeeRequest value) {
    if (value == null) return null;
    var result = new Employee();
    result.setId(value.getId());
    result.setEmployeeNo(value.getEmployeeNo());
    result.setName(value.getName());
    result.setDepartment(value.getDepartment());
    result.setDesignation(value.getDesignation());
    result.setMobile(value.getMobile());
    result.setStatus(value.getStatus());
    return result;
  }

  public static EmployeeResponse toResponse(Employee value) {
    if (value == null) return null;
    var result = new EmployeeResponse();
    result.setId(value.getId());
    result.setEmployeeNo(value.getEmployeeNo());
    result.setName(value.getName());
    result.setDepartment(value.getDepartment());
    result.setDesignation(value.getDesignation());
    result.setMobile(value.getMobile());
    result.setStatus(value.getStatus());
    return result;
  }
}
