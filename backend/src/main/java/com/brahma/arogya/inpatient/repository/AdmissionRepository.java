package com.brahma.arogya.inpatient.repository;

import com.brahma.arogya.inpatient.entity.Admission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {}
