package uk.gov.hmcts.reform.cpo.auditlog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class AuditLogFormatterTest implements BaseTest {

    private final AuditLogFormatter logFormatter = new AuditLogFormatter();

    @Test
    @DisplayName("Should have correct labels")
    void shouldHaveCorrectLabels() {

        // GIVEN
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setDateTime("2021-04-26 15:39:45");
        auditEntry.setOperationType("TEST_OPERATION_TYPE");
        auditEntry.setIdamId("test_idamId");
        auditEntry.setInvokingService("test_invokingService");
        auditEntry.setHttpMethod(HttpMethod.GET.name());
        auditEntry.setHttpStatus(HttpStatus.OK.value());
        auditEntry.setRequestPath(CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH);
        auditEntry.setCpoIds(List.of(CPO_ID_VALID_1));
        auditEntry.setCaseIds(List.of(CASE_ID_VALID_1));
        auditEntry.setRequestId(REQUEST_ID);

        // WHEN
        String result = logFormatter.format(auditEntry);

        // THEN
        assertEquals("Should have correct labels in full log format",
                     result,
                     AuditLogFormatter.TAG + " "
                         + "dateTime:2021-04-26 15:39:45,"
                         + "operationType:TEST_OPERATION_TYPE,"
                         + "idamId:test_idamId,"
                         + "invokingService:test_invokingService,"
                         + "endpointCalled:GET " + CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH + ","
                         + "operationalOutcome:200,"
                         + "cpoId:" + CPO_ID_VALID_1 + ","
                         + "caseId:" + CASE_ID_VALID_1 + ","
                         + "X-Request-ID:" + REQUEST_ID);
    }

    @Test
    @DisplayName("Should not log pair if empty")
    void shouldNotLogPairIfEmpty() {

        // GIVEN
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setDateTime("2021-04-26 15:39:45");
        auditEntry.setHttpMethod(HttpMethod.GET.name());
        auditEntry.setHttpStatus(HttpStatus.OK.value());
        auditEntry.setRequestPath(CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH);

        // WHEN
        String result = logFormatter.format(auditEntry);

        // THEN
        assertEquals("Should only log supplied pairs",
                     result,
                     AuditLogFormatter.TAG + " "
                         + "dateTime:2021-04-26 15:39:45,"
                         + "endpointCalled:GET " + CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH + ","
                         + "operationalOutcome:200");
    }

    @Test
    @DisplayName("Should handle lists with comma")
    void shouldHandleListsWithComma() {

        // GIVEN
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setDateTime("2021-04-26 15:39:45");
        auditEntry.setHttpMethod(HttpMethod.GET.name());
        auditEntry.setHttpStatus(HttpStatus.OK.value());
        auditEntry.setRequestPath(CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH);
        auditEntry.setCpoIds(List.of(CPO_ID_VALID_1, CPO_ID_VALID_2));
        auditEntry.setCaseIds(List.of(CASE_ID_VALID_1, CASE_ID_VALID_2));

        // WHEN
        String result = logFormatter.format(auditEntry);

        // THEN
        assertEquals("Should handle ID lists with comma",
                     result,
                     AuditLogFormatter.TAG + " "
                         + "dateTime:2021-04-26 15:39:45,"
                         + "endpointCalled:GET " + CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH + ","
                         + "operationalOutcome:200,"
                         + "cpoId:" + CPO_ID_VALID_1 + "," + CPO_ID_VALID_2 + ","
                         + "caseId:" + CASE_ID_VALID_1 + "," + CASE_ID_VALID_2);
    }

}
