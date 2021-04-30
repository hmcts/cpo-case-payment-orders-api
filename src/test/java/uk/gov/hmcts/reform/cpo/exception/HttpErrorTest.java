package uk.gov.hmcts.reform.cpo.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

class HttpErrorTest implements BaseTest {

    @Test
    @DisplayName("Should initialise HttpError with defaults")
    void shouldInitialiseHttpErrorWithDefaults() {

        // GIVEN
        RuntimeException ex = new RuntimeException();

        // WHEN
        HttpError<String> httpError = new HttpError<>(ex, CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH, null);

        // THEN
        assertEquals("Should record exception name", RuntimeException.class.getName(), httpError.getException());
        assertNotNull("Should record a date & time", httpError.getTimestamp());
        assertEquals("Should record path", CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH, httpError.getPath());
        assertEquals("Should record DEFAULT status", HttpError.DEFAULT_STATUS, httpError.getStatus());
        assertEquals("Should record DEFAULT error", HttpError.DEFAULT_ERROR, httpError.getError());
    }

    @Test
    @DisplayName("Should initialise HttpError from supplied HttpStatus")
    void shouldInitialiseHttpErrorFromSuppliedStatus() {

        // GIVEN
        RuntimeException ex = new RuntimeException();
        HttpStatus status = HttpStatus.I_AM_A_TEAPOT;

        // WHEN
        HttpError<String> httpError = new HttpError<>(ex, CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH, status);

        // THEN
        assertEquals("Should record supplied status", status.value(), httpError.getStatus());
        assertEquals("Should record supplied status as error", status.getReasonPhrase(), httpError.getError());
    }

    @Test
    @DisplayName("Should initialise HttpError from defaults if exception's ResponseStatus blank")
    void shouldInitialiseHttpErrorFromDefaultStatusIfExceptionsResponseStatusBlank() {

        // GIVEN
        TestResponseStatusThatsBlankException ex = new TestResponseStatusThatsBlankException("test");

        // WHEN
        HttpError<String> httpError =
            new HttpError<>(ex, CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH, null);

        // THEN
        assertEquals("Should record DEFAULT status", HttpError.DEFAULT_STATUS, httpError.getStatus());
        assertEquals("Should record DEFAULT error", HttpError.DEFAULT_ERROR, httpError.getError());
    }

    @Test
    @DisplayName("Should initialise HttpError from defaults and exception's ResponseStatus Reason")
    void shouldInitialiseHttpErrorFromDefaultStatusAndExceptionsResponseStatusReason() {

        // GIVEN
        TestResponseStatusWithReasonException ex = new TestResponseStatusWithReasonException("test");

        // WHEN
        HttpError<String> httpError =
            new HttpError<>(ex, CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH, null);

        // THEN
        assertEquals("Should record DEFAULT status",
                     HttpError.DEFAULT_STATUS, httpError.getStatus());
        assertEquals("Should record exception's ResponseStatus.Reason",
                     TestResponseStatusWithReasonException.REASON, httpError.getError());
    }

    @Test
    @DisplayName("Should initialise HttpError from exception's ResponseStatus Code")
    void shouldInitialiseHttpErrorFromExceptionsResponseStatusCode() {

        // GIVEN
        TestResponseStatusWithCodeException ex = new TestResponseStatusWithCodeException("test");

        // WHEN
        HttpError<String> httpError =
            new HttpError<>(ex, CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH, null);

        // THEN
        assertEquals("Should record exception's ResponseStatus Code",
                     TestResponseStatusWithCodeException.CODE.value(), httpError.getStatus());
        assertEquals("Should record exception's ResponseStatus Code as error",
                     TestResponseStatusWithCodeException.CODE.getReasonPhrase(), httpError.getError());
    }

    @Test
    @DisplayName("Should initialise HttpError from exception's ResponseStatus Value")
    void shouldInitialiseHttpErrorFromExceptionsResponseStatusValue() {

        // GIVEN
        TestResponseStatusWithValueException ex = new TestResponseStatusWithValueException("test");

        // WHEN
        HttpError<String> httpError =
            new HttpError<>(ex, CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH, null);

        // THEN
        assertEquals("Should record exception's ResponseStatus Code",
                     TestResponseStatusWithValueException.VALUE.value(), httpError.getStatus());
        assertEquals("Should record exception's ResponseStatus Code as error",
                     TestResponseStatusWithValueException.VALUE.getReasonPhrase(), httpError.getError());
    }

    @ResponseStatus()
    private static class TestResponseStatusThatsBlankException extends ApiException {
        public TestResponseStatusThatsBlankException(String message) {
            super(message);
        }
    }

    @ResponseStatus(reason = TestResponseStatusWithReasonException.REASON)
    private static class TestResponseStatusWithReasonException extends ApiException {
        public static final String REASON = "Test Reason";

        public TestResponseStatusWithReasonException(String message) {
            super(message);
        }
    }

    @ResponseStatus(code = HttpStatus.I_AM_A_TEAPOT)
    private static class TestResponseStatusWithCodeException extends ApiException {
        public static final HttpStatus CODE = HttpStatus.I_AM_A_TEAPOT;

        public TestResponseStatusWithCodeException(String message) {
            super(message);
        }
    }

    @ResponseStatus(value = HttpStatus.I_AM_A_TEAPOT)
    private static class TestResponseStatusWithValueException extends ApiException {
        public static final HttpStatus VALUE = HttpStatus.I_AM_A_TEAPOT;

        public TestResponseStatusWithValueException(String message) {
            super(message);
        }
    }
}
