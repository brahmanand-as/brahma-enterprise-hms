package com.brahma.arogya.hr.repository;

import com.brahma.arogya.hr.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {}
