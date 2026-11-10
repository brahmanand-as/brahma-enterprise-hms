package com.brahma.arogya.insurance.repository;

import com.brahma.arogya.insurance.entity.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {}
