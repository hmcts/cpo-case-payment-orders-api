package uk.gov.hmcts.reform.cpo.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdsValidatorTest {

    public static final String INVALID_UUID = "invalidUuid";

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    private final IdsValidator idsValidator = new IdsValidator();

    @Test
    void testIsValidReturnsTrueWhenCaseIdsEmpty() {
        assertTrue(idsValidator.isValid(Optional.empty(), constraintValidatorContext));
    }

    @Test
    void testIsValidReturnsTrueWhenIdsAllValid() {
        assertTrue(idsValidator.isValid(
                Optional.of(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString())),
                constraintValidatorContext));
    }

    @Test
    void testIsValidReturnsFalseWhenIdIsInvalid() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(constraintViolationBuilder);
        assertFalse(idsValidator.isValid(
                Optional.of(List.of(UUID.randomUUID().toString(), INVALID_UUID)), constraintValidatorContext));
    }

    @Test
    void testValidateReturnsListOfErrorsWhenIdIsInvalid() {
        List<String> errors = new ArrayList<>();
        idsValidator.validate(INVALID_UUID, errors);
        assertEquals(1, errors.size());
    }

    @Test
    void testValidateReturnsEmptyListOfErrorsWhenIdIsValid() {
        List<String> errors = new ArrayList<>();
        idsValidator.validate(UUID.randomUUID().toString(), errors);
        assertTrue(errors.isEmpty());
    }

}
