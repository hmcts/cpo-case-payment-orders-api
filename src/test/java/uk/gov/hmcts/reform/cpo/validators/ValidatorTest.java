package uk.gov.hmcts.reform.cpo.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

class ValidatorTest {

    @Nested
    @DisplayName("Validator.isValidCaseId(..)")
    class IsValidCaseIdTests {

        @DisplayName("successfully validates a valid Case ID")
        @Test
        void successfullyValidatesValidCaseId() {
            assertTrue(
                "16 digit Luhn number should validate as true",
                Validator.isValidCaseId("4444333322221111")
            );
        }

        @DisplayName("successfully flags null Case ID as invalid")
        @Test
        void successfullyFlagsNullCaseIdAsInvalid() {
            assertFalse(
                "Null value Case ID should be flagged as invalid",
                Validator.isValidCaseId(null)
            );
        }

        @DisplayName("successfully flags empty Case ID as invalid")
        @Test
        void successfullyFlagsEmptyCaseIdAsInvalid() {
            assertFalse(
                "Empty value Case ID should be flagged as invalid",
                Validator.isValidCaseId("")
            );
        }

        @DisplayName("successfully flags non-numeric Case ID as invalid")
        @Test
        void successfullyFlagsNonNumericCaseIdAsInvalid() {
            assertFalse(
                "non-numeric Case ID should be flagged as invalid",
                Validator.isValidCaseId("NON-NUMERIC")
            );
        }

        @DisplayName("successfully flags short Case ID as invalid")
        @Test
        void successfullyFlagsShortCaseIdAsInvalid() {
            assertFalse(
                "Short Case ID should be flagged as invalid",
                Validator.isValidCaseId("3038") // NB: valid luhn number
            );
        }

        @DisplayName("successfully flags bad-luhn Case ID as invalid")
        @Test
        void successfullyFlagsBadLuhnCaseIdAsInvalid() {
            assertFalse(
                "non-numeric Case ID should be flagged as invalid",
                Validator.isValidCaseId("4444333322221110") // NB: correct length: 16 digits
            );
        }

    }

    @Nested
    @DisplayName("Validator.isValidCpoId(..)")
    class IsValidCpoIdTests {

        @DisplayName("successfully validates a valid CPO ID")
        @Test
        void successfullyValidatesValidCpoId() {
            assertTrue(
                "Random UUID should validate as true",
                Validator.isValidCpoId(UUID.randomUUID().toString())
            );
        }

        @DisplayName("successfully flags null CPO ID as invalid")
        @Test
        void successfullyFlagsNullCpoIdAsInvalid() {
            assertFalse(
                "Null CPO ID should be flagged as invalid",
                Validator.isValidCpoId(null)
            );
        }

        @DisplayName("successfully flags empty CPO ID as invalid")
        @Test
        void successfullyFlagsEmptyCpoIdAsInvalid() {
            assertFalse(
                "Empty CPO ID should be flagged as invalid",
                Validator.isValidCpoId("")
            );
        }

        @DisplayName("successfully flags non-numeric CPO ID as invalid")
        @Test
        void successfullyFlagsNonNumericCpoIdAsInvalid() {
            assertFalse(
                "non-numeric CPO ID should be flagged as invalid",
                Validator.isValidCpoId("NON-NUMERIC")
            );
        }

        @DisplayName("successfully flags bad-UUID CPO ID as invalid")
        @Test
        void successfullyFlagsBadUuidCpoIdAsInvalid() {
            assertFalse(
                "Bad UUID CPO ID should be flagged as invalid",
                Validator.isValidCpoId("123456")
            );
        }

    }

}
