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

class IdTest implements BaseTest {

    private IdValidator idValidator = new IdValidator();

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("successfully validates a valid CPO ID")
    @Test
    void passForValidId() {
        final boolean result = idValidator.isValid(CPO_ID_VALID_1, constraintValidatorContext);
        assertTrue("The values: " + CPO_ID_VALID_1 + " should be valid", result);
    }

    @DisplayName("successfully flags null CPO ID as valid")
    @Test
    void passForNullId() {
        final boolean result = idValidator.isValid(null, constraintValidatorContext);
        assertTrue("The empty value should be valid", result); // NB: is required checked elsewhere
    }

    @DisplayName("successfully flags empty CPO ID as valid")
    @Test
    void passForEmptyId() {
        final boolean result = idValidator.isValid("", constraintValidatorContext);
        assertTrue("The empty value should be valid", result); // NB: is required checked elsewhere
    }

    @DisplayName("successfully flags non-numeric CPO ID as invalid")
    @Test
    void failForInvalidIdNonNumeric() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(
            constraintViolationBuilder);
        final boolean result = idValidator.isValid(CPO_ID_INVALID_NON_NUMERIC, constraintValidatorContext);
        assertFalse("The values: " + CPO_ID_INVALID_NON_NUMERIC + " should not be valid", result);
    }

    @DisplayName("successfully flags bad-UUID CPO ID as invalid")
    @Test
    void failForInvalidIdBadUuid() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(
            constraintViolationBuilder);
        final boolean result = idValidator.isValid(CPO_ID_INVALID_1, constraintValidatorContext);
        assertFalse("The values: " + CPO_ID_INVALID_1 + " should not be valid", result);
    }

}
