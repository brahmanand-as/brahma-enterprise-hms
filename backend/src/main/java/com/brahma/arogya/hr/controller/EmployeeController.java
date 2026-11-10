package com.brahma.arogya.hr.controller;

import com.brahma.arogya.hr.dto.EmployeeRequest;
import com.brahma.arogya.hr.dto.EmployeeResponse;
import com.brahma.arogya.hr.mapper.EmployeeMapper;
import com.brahma.arogya.hr.service.EmployeeService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

  private final EmployeeService service;

  public EmployeeController(EmployeeService service) {
    this.service = service;
  }

  @GetMapping
  public List<EmployeeResponse> all() {
    return service.all().stream().map(EmployeeMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public EmployeeResponse one(@PathVariable Long id) {
    return EmployeeMapper.toResponse(service.one(id));
  }

  @PostMapping
  public EmployeeResponse create(@RequestBody @jakarta.validation.Valid() EmployeeRequest x) {
    return EmployeeMapper.toResponse(service.create(EmployeeMapper.toEntity(x)));
  }

  @PutMapping("/{id}")
  public EmployeeResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() EmployeeRequest x) {
    return EmployeeMapper.toResponse(service.update(id, EmployeeMapper.toEntity(x)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
