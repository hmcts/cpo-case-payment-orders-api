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

class IdsTest implements BaseTest<String> {

    private IdsValidator idsValidator = new IdsValidator();

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void passForValidIds() {
        final String[] testedData = {CPO_ID_VALID_1, CPO_ID_VALID_2, CPO_ID_VALID_3};

        final Optional<List<String>> valuesToBeTested = createInitialValuesList(testedData);
        final boolean result = idsValidator.isValid(valuesToBeTested, constraintValidatorContext);
        assertTrue("The values: " + testedData + " should be valid", result);
    }

    @Test
    void passForEmptyIds() {
        final Optional<List<String>> valuesToBeTested = Optional.empty();
        final boolean result = idsValidator.isValid(valuesToBeTested, constraintValidatorContext);
        assertTrue("The empty value should be valid", result);
    }

    @Test
    void passForOneId() {
        final List<String> errors = new ArrayList<String>();
        final String valueToBeTested = CPO_ID_VALID_1;
        idsValidator.validate(valueToBeTested, errors);
        assertTrue("There should not be any error for " + valueToBeTested + " value.", errors.isEmpty());
    }

    @Test
    void failForInvalidIds() {
        final String[] testedData = {CPO_ID_INVALID_NON_NUMERIC, CPO_ID_INVALID_1, CPO_ID_INVALID_2};
        final Optional<List<String>> valuesToBeTested = createInitialValuesList(testedData);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(
            constraintViolationBuilder);
        final boolean result = idsValidator.isValid(valuesToBeTested, constraintValidatorContext);
        assertFalse("The values: " + testedData + " should not be valid", result);
    }

    @Test
    void failForOneId() {
        final List<String> errors = new ArrayList<String>();
        final String valueToBeTested = CPO_ID_INVALID_1;
        idsValidator.validate(valueToBeTested, errors);
        assertTrue("There should be any error for " + valueToBeTested + " value.", !errors.isEmpty());
    }
}
