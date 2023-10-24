package uk.gov.hmcts.reform.cpo.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.BaseTest;

import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;


class CaseIdsTest implements BaseTest {

    private final CaseIdsValidator caseIdsValidator = new CaseIdsValidator();

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void passForValidCasesIds() {
        final String[] testedData = {CASE_ID_VALID_1, CASE_ID_VALID_2, CASE_ID_VALID_3};
        final Optional<List<String>> valuesToBeTested = createInitialValuesList(testedData);
        final boolean result = caseIdsValidator.isValid(valuesToBeTested, constraintValidatorContext);
        assertTrue("The values: " + Arrays.toString(testedData) + " should be valid", result);
    }

    @Test
    void passForEmptyCasesIds() {
        final Optional<List<String>> valuesToBeTested = Optional.empty();
        final boolean result = caseIdsValidator.isValid(valuesToBeTested, constraintValidatorContext);
        assertTrue("The empty value should be valid", result);
    }

    @Test
    void passForOneCasesId() {
        final List<String> errors = new ArrayList<String>();
        final String valueToBeTested = CASE_ID_VALID_1;
        caseIdsValidator.validate(valueToBeTested, errors);
        assertTrue("There should not be any error for " + valueToBeTested + " value.", errors.isEmpty());
    }

    @Test
    void failForInvalidCasesIds() {
        final String[] testedData = {CASE_ID_INVALID_NON_NUMERIC, CASE_ID_INVALID_LUHN, CASE_ID_INVALID_LENGTH};
        final Optional<List<String>> valuesToBeTested = createInitialValuesList(testedData);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(
            constraintViolationBuilder);
        final boolean result = caseIdsValidator.isValid(valuesToBeTested, constraintValidatorContext);
        assertFalse("The values: " + Arrays.toString(testedData) + " should not be valid", result);
    }

    @Test
    void failForOneCasesId() {
        final List<String> errors = new ArrayList<String>();
        final String valueToBeTested = CASE_ID_INVALID_LENGTH;
        caseIdsValidator.validate(valueToBeTested, errors);
        assertTrue("There should be any error for " + valueToBeTested + " value.", !errors.isEmpty());
    }

}
