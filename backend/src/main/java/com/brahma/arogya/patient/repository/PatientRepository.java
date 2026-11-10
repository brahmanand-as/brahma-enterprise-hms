package com.brahma.arogya.patient.repository;

import com.brahma.arogya.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {}
