package uk.gov.hmcts.reform.cpo.auditlog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.AssertionErrors.assertEquals;

class AuditLogFormatterTest implements BaseTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private AuditLogFormatter logFormatter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        logFormatter = new AuditLogFormatter(0);
    }

    @Test
    @DisplayName("Should have correct labels")
    void shouldHaveCorrectLabels() throws Exception {

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
        JsonNode json = objectMapper.readTree(result);

        // THEN
        assertEquals("Should write tag", AuditLogFormatter.TAG, json.get("tag").asText());
        assertEquals("Should write dateTime", "2021-04-26 15:39:45", json.get("dateTime").asText());
        assertEquals("Should write operation type", "TEST_OPERATION_TYPE", json.get("operationType").asText());
        assertEquals("Should write idamId", "test_idamId", json.get("idamId").asText());
        assertEquals("Should write invokingService", "test_invokingService", json.get("invokingService").asText());
        assertEquals("Should write endpoint",
                     "GET " + CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH,
                     json.get("endpointCalled").asText());
        assertEquals("Should write status", 200, json.get("operationalOutcome").asInt());
        assertThat(json.get("cpoId").size()).isEqualTo(1);
        assertEquals("Should write cpoId", CPO_ID_VALID_1, json.get("cpoId").get(0).asText());
        assertThat(json.get("caseId").size()).isEqualTo(1);
        assertEquals("Should write caseId", CASE_ID_VALID_1, json.get("caseId").get(0).asText());
        assertEquals("Should write request id", REQUEST_ID, json.get("X-Request-ID").asText());
    }

    @Test
    @DisplayName("Should not log pair if empty")
    void shouldNotLogPairIfEmpty() throws Exception {

        // GIVEN
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setDateTime("2021-04-26 15:39:45");
        auditEntry.setHttpMethod(HttpMethod.GET.name());
        auditEntry.setHttpStatus(HttpStatus.OK.value());
        auditEntry.setRequestPath(CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH);

        // WHEN
        String result = logFormatter.format(auditEntry);
        JsonNode json = objectMapper.readTree(result);

        // THEN
        assertEquals("Should write tag", AuditLogFormatter.TAG, json.get("tag").asText());
        assertEquals("Should write dateTime", "2021-04-26 15:39:45", json.get("dateTime").asText());
        assertEquals("Should write endpoint",
                     "GET " + CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH,
                     json.get("endpointCalled").asText());
        assertEquals("Should write status", 200, json.get("operationalOutcome").asInt());
        assertThat(json.has("operationType")).isFalse();
        assertThat(json.has("idamId")).isFalse();
    }

    @Test
    @DisplayName("Should handle lists with comma")
    void shouldHandleListsWithComma() throws Exception {

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
        JsonNode json = objectMapper.readTree(result);

        // THEN
        assertThat(json.get("cpoId").size()).isEqualTo(2);
        assertEquals("Should keep first cpoId", CPO_ID_VALID_1, json.get("cpoId").get(0).asText());
        assertEquals("Should keep second cpoId", CPO_ID_VALID_2, json.get("cpoId").get(1).asText());
        assertThat(json.get("caseId").size()).isEqualTo(2);
        assertEquals("Should keep first caseId", CASE_ID_VALID_1, json.get("caseId").get(0).asText());
        assertEquals("Should keep second caseId", CASE_ID_VALID_2, json.get("caseId").get(1).asText());
    }

    @Test
    @DisplayName("Should handle lists with limit")
    void shouldHandleListsWithLimit() throws Exception {

        // GIVEN
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setDateTime("2021-04-26 15:39:45");
        auditEntry.setHttpMethod(HttpMethod.GET.name());
        auditEntry.setHttpStatus(HttpStatus.OK.value());
        auditEntry.setRequestPath(CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH);
        auditEntry.setCpoIds(List.of(CPO_ID_VALID_1, CPO_ID_VALID_2, CPO_ID_VALID_3));
        auditEntry.setCaseIds(List.of(CASE_ID_VALID_1, CASE_ID_VALID_2, CASE_ID_VALID_3));

        int auditLogMaxListSize = 2;
        logFormatter = new AuditLogFormatter(auditLogMaxListSize);

        // WHEN
        String result = logFormatter.format(auditEntry);
        JsonNode json = objectMapper.readTree(result);

        // THEN
        assertThat(json.get("cpoId").size()).isEqualTo(2);
        assertEquals("Should keep first cpoId", CPO_ID_VALID_1, json.get("cpoId").get(0).asText());
        assertEquals("Should keep second cpoId", CPO_ID_VALID_2, json.get("cpoId").get(1).asText());
        assertThat(json.get("caseId").size()).isEqualTo(2);
        assertEquals("Should keep first caseId", CASE_ID_VALID_1, json.get("caseId").get(0).asText());
        assertEquals("Should keep second caseId", CASE_ID_VALID_2, json.get("caseId").get(1).asText());
    }

}
