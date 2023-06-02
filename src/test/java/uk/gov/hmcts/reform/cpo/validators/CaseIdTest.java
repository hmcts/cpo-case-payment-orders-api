package uk.gov.hmcts.reform.cpo.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.BaseTest;

import jakarta.validation.ConstraintValidatorContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;


class CaseIdTest implements BaseTest {

    private CaseIdValidator caseIdValidator = new CaseIdValidator();

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("successfully validates a valid Case ID")
    @Test
    void passForValidCasesId() {
        final boolean result = caseIdValidator.isValid(CASE_ID_VALID_1, constraintValidatorContext);
        assertTrue("The value: " + CASE_ID_VALID_1 + " should be valid", result);
    }

    @DisplayName("successfully flags null Case ID as valid")
    @Test
    void passForNullCasesId() {
        final boolean result = caseIdValidator.isValid(null, constraintValidatorContext);
        assertTrue("The empty value should be valid", result); // NB: is required checked elsewhere
    }

    @DisplayName("successfully flags empty Case ID as valid")
    @Test
    void passForEmptyCasesId() {
        final boolean result = caseIdValidator.isValid("", constraintValidatorContext);
        assertTrue("The empty value should be valid", result); // NB: is required checked elsewhere
    }

    @DisplayName("successfully flags non-numeric Case ID as invalid")
    @Test
    void failForInvalidCasesIdNonNumeric() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(
            constraintViolationBuilder);
        final boolean result = caseIdValidator.isValid(CASE_ID_INVALID_NON_NUMERIC, constraintValidatorContext);
        assertFalse("The value: " + CASE_ID_INVALID_NON_NUMERIC + " should not be valid", result);
    }

    @DisplayName("successfully flags short Case ID as invalid")
    @Test
    void failForInvalidCasesIdShort() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(
            constraintViolationBuilder);
        final boolean result = caseIdValidator.isValid(CASE_ID_INVALID_LENGTH, constraintValidatorContext);
        assertFalse("The value: " + CASE_ID_INVALID_LENGTH + " should not be valid", result);
    }

    @DisplayName("successfully flags bad-luhn Case ID as invalid")
    @Test
    void failForInvalidCasesIdBadLuhn() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(
            constraintViolationBuilder);
        final boolean result = caseIdValidator.isValid(CASE_ID_INVALID_LUHN, constraintValidatorContext);
        assertFalse("The value: " + CASE_ID_INVALID_LUHN + " should not be valid", result);
    }

}
