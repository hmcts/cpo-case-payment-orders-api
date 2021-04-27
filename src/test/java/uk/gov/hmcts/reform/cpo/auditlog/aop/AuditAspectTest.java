package uk.gov.hmcts.reform.cpo.auditlog.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.stereotype.Controller;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.auditlog.AuditOperationType;
import uk.gov.hmcts.reform.cpo.auditlog.LogAudit;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

class AuditAspectTest implements BaseTest {

    private final AuditAspect aspect = new AuditAspect();
    private TestController controllerProxy;

    @BeforeEach
    void setUp() {
        AspectJProxyFactory aspectJProxyFactory = new AspectJProxyFactory(new TestController());
        aspectJProxyFactory.addAspect(aspect);

        DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
        AopProxy aopProxy = proxyFactory.createAopProxy(aspectJProxyFactory);

        controllerProxy = (TestController) aopProxy.getProxy();
    }

    @Test
    @DisplayName("Should populate audit context")
    void shouldPopulateAuditContext() {

        // WHEN
        CasePaymentOrder result = controllerProxy.createCpo();
        AuditContext context = AuditContextHolder.getAuditContext();

        // THEN
        assertNotNull("Context should exist", context);
        assertEquals("OperationType should be populated as per LogAudit",
                     AuditOperationType.CREATE_CASE_PAYMENT_ORDER, context.getAuditOperationType());
        // single ID values
        assertEquals("CPO ID should be populated as per LogAudit path",
                     result.getId().toString(), context.getCpoId());
        assertEquals("Case ID should be populated as per LogAudit path",
                     result.getCaseId().toString(), context.getCaseId());
        // lists of ID Values
        assertNull("CPO ID list should be populated as per LogAudit path",
                   context.getCpoIds());
        assertNull("Case ID List should be populated as per LogAudit path",
                   context.getCaseIds());
    }

    @Test
    @DisplayName("Should populate audit context even when method execution returns an error")
    void shouldPopulateAuditContextEvenWhenMethodExecutionReturnsError() {
        // GIVEN
        List<String> cpoIds = List.of(CPO_ID_VALID_1, CPO_ID_VALID_2);
        List<String> caseIds = List.of(CASE_ID_VALID_1, CASE_ID_VALID_2);

        // WHEN
        // NB: `getCpos` is configured to throw error
        assertThrows(RuntimeException.class, () -> controllerProxy.getCpos(cpoIds, caseIds));
        AuditContext context = AuditContextHolder.getAuditContext();

        // THEN
        assertNotNull("Context should exist", context);
        assertEquals("OperationType should be populated as per LogAudit",
                     AuditOperationType.GET_CASE_PAYMENT_ORDER, context.getAuditOperationType());
        // single ID values
        assertNull("CPO ID should be populated as per LogAudit path",
                     context.getCpoId());
        assertNull("Case ID should be populated as per LogAudit path",
                     context.getCaseId());
        // lists of ID Values
        assertTrue("CPO ID list should be populated as per LogAudit path",
                     context.getCpoIds().containsAll(cpoIds));
        assertTrue("Case ID List should be populated as per LogAudit path",
                     context.getCaseIds().containsAll(caseIds));

    }

    @Test
    @DisplayName("Should populate audit context even when method execution returns an error")
    void shouldPopulateAuditContextEvenWhenLogAuditValueProcessingErrors() {
        // GIVEN
        UpdateCasePaymentOrderRequest updateRequest = createUpdateCasePaymentOrderRequest();

        // WHEN
        // NB: `updateCpo` is configured with a bad LogAudit.cpoIds value expression
        controllerProxy.updateCpo(updateRequest);
        AuditContext context = AuditContextHolder.getAuditContext();

        // THEN
        assertNotNull("Context should exist", context);
        assertEquals("OperationType should be populated as per LogAudit",
                     AuditOperationType.UPDATE_CASE_PAYMENT_ORDER, context.getAuditOperationType());
        // single ID values
        assertEquals("CPO ID should be populated as per LogAudit path",
                     updateRequest.getId(), context.getCpoId());
        assertEquals("Case ID should be populated as per LogAudit path",
                     updateRequest.getCaseId(), context.getCaseId());
        // lists of ID Values
        assertNull("CPO ID list should be null as LogAudit path is bad",
                   context.getCpoIds());
        assertNull("Case ID List should be populated as per LogAudit path",
                   context.getCaseIds());
    }

    @Controller
    @SuppressWarnings("unused")
    public static class TestController {

        @LogAudit(
            operationType = AuditOperationType.CREATE_CASE_PAYMENT_ORDER,
            cpoId = "#result.id",
            caseId = "#result.caseId"
        )
        public CasePaymentOrder createCpo() {
            return CasePaymentOrder.builder()
                .id(UUID.fromString(CPO_ID_VALID_1))
                .caseId(Long.parseLong(CASE_ID_VALID_1))
                .build();
        }

        @SuppressWarnings("UnusedReturnValue")
        @LogAudit(
            operationType = AuditOperationType.GET_CASE_PAYMENT_ORDER,
            cpoIds = "#cpoIds",
            caseIds = "#caseIds"
        )
        public CasePaymentOrder getCpos(List<String> cpoIds,
                                        List<String> caseIds) {
            throw new RuntimeException("get case failed");
        }

        @SuppressWarnings("UnusedReturnValue")
        @LogAudit(
            operationType = AuditOperationType.UPDATE_CASE_PAYMENT_ORDER,
            cpoId = "#updateRequest.id",
            caseId = "#updateRequest.caseId",
            cpoIds = "#bad-path"
        )
        public CasePaymentOrder updateCpo(UpdateCasePaymentOrderRequest updateRequest) {
            return CasePaymentOrder.builder()
                .id(UUID.fromString(CPO_ID_VALID_1))
                .caseId(Long.parseLong(CASE_ID_VALID_1))
                .build();
        }
    }
}
