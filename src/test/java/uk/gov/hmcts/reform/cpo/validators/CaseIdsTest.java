package uk.gov.hmcts.reform.cpo.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.BaseTest;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;


class CaseIdsTest implements BaseTest<String> {

    private CaseIdsValidator caseIdsValidator = new CaseIdsValidator();
    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void passForValidCasesIds() {
        final String[] testedData = {"1609243447569251", "1609243447569252", "1609243447569253"};
        final Optional<List<String>> valuesToBeTested = createInitialValuesList(testedData);
        final boolean result = caseIdsValidator.isValid(valuesToBeTested, constraintValidatorContext);
        assertTrue("The values: " + testedData + "should be valid", result);
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
        final String valueToBeTested = "1609243447569251";
        caseIdsValidator.validate(valueToBeTested, errors);
        assertTrue("There should not be any error for " + valueToBeTested + " value.", errors.isEmpty());
    }

    @Test
    void failForInvalidCasesIds() {
        final String[] testedData = {"dddd", "160924", "160924 "};
        final Optional<List<String>> valuesToBeTested = createInitialValuesList(testedData);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(
            constraintViolationBuilder);
        final boolean result = caseIdsValidator.isValid(valuesToBeTested, constraintValidatorContext);
        assertFalse("The values: " + testedData + "should not be valid", result);
    }

    @Test
    void failForOneCasesId() {
        final List<String> errors = new ArrayList<String>();
        final String valueToBeTested = "1609";
        caseIdsValidator.validate(valueToBeTested, errors);
        assertTrue("There should be any error for " + valueToBeTested + " value.", !errors.isEmpty());
    }
}
