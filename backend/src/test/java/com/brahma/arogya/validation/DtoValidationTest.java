package com.brahma.arogya.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.brahma.arogya.clinical.dto.VitalRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyDispenseRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyPurchaseLineRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyPurchaseRequest;
import com.brahma.arogya.portal.dto.EnterprisePortalRegistrationRequest;
import jakarta.validation.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class DtoValidationTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validSyntheticRegistrationPasses() {
    assertTrue(
        validator
            .validate(
                new EnterprisePortalRegistrationRequest(
                    "test.patient", "TestPatient2026!", "Synthetic", "Patient", "9000000001"))
            .isEmpty());
  }

  @Test
  void emailUsernameShortPasswordAndMalformedMobileReturnFieldErrors() {
    var errors =
        validator.validate(
            new EnterprisePortalRegistrationRequest(
                "test@example.test", "short", "Synthetic", "Patient", "123"));
    assertTrue(errors.stream().anyMatch(e -> e.getPropertyPath().toString().equals("username")));
    assertTrue(errors.stream().anyMatch(e -> e.getPropertyPath().toString().equals("password")));
    assertTrue(errors.stream().anyMatch(e -> e.getPropertyPath().toString().equals("mobile")));
  }

  @Test
  void prescriptionAndIpdOrderRemainOptionalAlternatives() {
    assertTrue(
        validator
            .validate(new PharmacyDispenseRequest(1L, 2L, null, 3L, BigDecimal.ONE))
            .isEmpty());
    assertTrue(
        validator
            .validate(new PharmacyDispenseRequest(1L, null, 2L, 3L, BigDecimal.ONE))
            .isEmpty());
  }

  @Test
  void negativeQuantityAndMissingPatientAreRejected() {
    assertFalse(
        validator
            .validate(new PharmacyDispenseRequest(null, 2L, null, 3L, BigDecimal.valueOf(-1)))
            .isEmpty());
  }

  @Test
  void nestedPurchaseLinesAreValidated() {
    var request =
        new PharmacyPurchaseRequest(
            1L,
            List.of(new PharmacyPurchaseLineRequest(2L, BigDecimal.valueOf(-1), BigDecimal.ONE)));
    assertTrue(
        validator.validate(request).stream()
            .anyMatch(e -> e.getPropertyPath().toString().contains("quantity")));
  }

  @Test
  void spo2OutsideExistingBoundsIsRejected() {
    var vital = new VitalRequest();
    vital.setSpo2(new BigDecimal("101"));
    assertFalse(validator.validate(vital).isEmpty());
  }

  @Test
  void returnCreditWithoutCashRefundDoesNotRequirePaymentDetails() {
    assertTrue(
        validator
            .validate(new com.brahma.arogya.billing.dto.FinancialCreditReconcileRequest(null, null))
            .isEmpty());
  }
}
