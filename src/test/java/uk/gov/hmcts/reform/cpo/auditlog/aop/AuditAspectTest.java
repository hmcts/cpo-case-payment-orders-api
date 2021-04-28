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
    @DisplayName("Should populate audit context when single IDs")
    @SuppressWarnings("VariableDeclarationUsageDistance")
    void shouldPopulateAuditContextWhenSingleIds() {

        // WHEN
        CasePaymentOrder result = controllerProxy.createCpo_LogSingleIds();
        AuditContext context = AuditContextHolder.getAuditContext();

        // THEN
        assertNotNull("Context should exist", context);
        assertEquals("OperationType should be populated as per LogAudit",
                     AuditOperationType.CREATE_CASE_PAYMENT_ORDER, context.getAuditOperationType());

        assertEquals("CPO ID list should be populated as per LogAudit path: size check",
                     1, context.getCpoIds().size());
        assertEquals("CPO ID list should be populated as per LogAudit path: value check",
                     result.getId().toString(), context.getCpoIds().get(0));

        assertEquals("Case ID list should be populated as per LogAudit path: size check",
                     1, context.getCaseIds().size());
        assertEquals("Case ID list should be populated as per LogAudit path: value check",
                     result.getCaseId().toString(), context.getCaseIds().get(0));

    }

    @Test
    @DisplayName("Should populate audit context when list of IDs")
    void shouldPopulateAuditContextWhenListOfIds() {

        // GIVEN
        List<String> cpoIds = List.of(CPO_ID_VALID_1, CPO_ID_VALID_2);
        List<String> caseIds = List.of(CASE_ID_VALID_1, CASE_ID_VALID_2);

        // WHEN
        controllerProxy.getCpos_LogListOfIds(cpoIds, caseIds);
        AuditContext context = AuditContextHolder.getAuditContext();

        // THEN
        assertNotNull("Context should exist", context);
        assertEquals("OperationType should be populated as per LogAudit",
                     AuditOperationType.GET_CASE_PAYMENT_ORDER, context.getAuditOperationType());

        assertEquals("CPO ID list should be populated as per LogAudit path: size check",
                     cpoIds.size(), context.getCpoIds().size());
        assertTrue("CPO ID list should be populated as per LogAudit path: value check",
                     context.getCpoIds().containsAll(cpoIds));

        assertEquals("Case ID list should be populated as per LogAudit path: size check",
                     caseIds.size(), context.getCaseIds().size());
        assertTrue("Case ID list should be populated as per LogAudit path: value check",
                     context.getCaseIds().containsAll(caseIds));

    }

    @Test
    @DisplayName("Should populate audit context when single and list of IDs")
    void shouldPopulateAuditContexWhenSingleAndListOfIds() {

        // GIVEN
        List<String> cpoIds = List.of(CPO_ID_VALID_1, CPO_ID_VALID_2);
        List<String> caseIds = List.of(CASE_ID_VALID_1, CASE_ID_VALID_2);
        String cpoId = CPO_ID_VALID_3;
        String caseId = CASE_ID_VALID_3;

        // WHEN
        controllerProxy.deleteCpos_LogSingleIdsAndListOfIds(cpoIds, caseIds, cpoId, caseId);
        AuditContext context = AuditContextHolder.getAuditContext();

        // THEN
        assertNotNull("Context should exist", context);
        assertEquals("OperationType should be populated as per LogAudit",
                     AuditOperationType.DELETE_CASE_PAYMENT_ORDER, context.getAuditOperationType());

        assertEquals("CPO ID list should be populated as per LogAudit path: size check",
                     cpoIds.size() + 1, context.getCpoIds().size());
        assertTrue("CPO ID list should be populated as per LogAudit path: value check, part 1",
                   context.getCpoIds().containsAll(cpoIds));
        assertTrue("CPO ID list should be populated as per LogAudit path: value check, part 2",
                   context.getCpoIds().contains(cpoId));

        assertEquals("Case ID list should be populated as per LogAudit path: size check",
                     caseIds.size() + 1, context.getCaseIds().size());
        assertTrue("Case ID list should be populated as per LogAudit path: value check, part 1",
                   context.getCaseIds().containsAll(caseIds));
        assertTrue("Case ID list should be populated as per LogAudit path: value check, part 2",
                   context.getCaseIds().contains(caseId));

    }

    @Test
    @DisplayName("Should populate audit context even when method execution returns an error")
    void shouldPopulateAuditContextEvenWhenMethodExecutionReturnsError() {
        // GIVEN
        List<String> cpoIds = List.of(CPO_ID_VALID_1, CPO_ID_VALID_2);
        List<String> caseIds = List.of(CASE_ID_VALID_1, CASE_ID_VALID_2);

        // WHEN
        assertThrows(RuntimeException.class, () -> controllerProxy.deleteCpos_ThrowError(cpoIds, caseIds));
        AuditContext context = AuditContextHolder.getAuditContext();

        // THEN
        assertNotNull("Context should exist", context);
        assertEquals("OperationType should be populated as per LogAudit",
                     AuditOperationType.DELETE_CASE_PAYMENT_ORDER, context.getAuditOperationType());

        assertTrue("CPO ID list should be populated as per LogAudit path",
                     context.getCpoIds().containsAll(cpoIds));
        assertTrue("Case ID List should be populated as per LogAudit path",
                     context.getCaseIds().containsAll(caseIds));

    }

    @Test
    @DisplayName("Should populate audit context even when log audot value processing errors")
    void shouldPopulateAuditContextEvenWhenLogAuditValueProcessingErrors() {
        // GIVEN
        UpdateCasePaymentOrderRequest updateRequest = createUpdateCasePaymentOrderRequest();

        // WHEN
        controllerProxy.updateCpo_LogUsingBadPath(updateRequest);
        AuditContext context = AuditContextHolder.getAuditContext();

        // THEN
        assertNotNull("Context should exist", context);
        assertEquals("OperationType should be populated as per LogAudit",
                     AuditOperationType.UPDATE_CASE_PAYMENT_ORDER, context.getAuditOperationType());

        assertTrue("CPO ID list should be populated as per LogAudit path",
                   context.getCpoIds().contains(updateRequest.getId()));
        assertTrue("Case ID List should be populated as per LogAudit path",
                   context.getCaseIds().contains(updateRequest.getCaseId()));

    }

    @Controller
    @SuppressWarnings("unused")
    public static class TestController {

        @LogAudit(
            operationType = AuditOperationType.CREATE_CASE_PAYMENT_ORDER,
            cpoId = "#result.id",
            caseId = "#result.caseId"
        )
        public CasePaymentOrder createCpo_LogSingleIds() {
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
        public List<CasePaymentOrder> getCpos_LogListOfIds(List<String> cpoIds,
                                                           List<String> caseIds) {
            return List.of();
        }

        @LogAudit(
            operationType = AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
            cpoIds = "#cpoIds",
            caseIds = "#caseIds",
            cpoId = "#cpoId",
            caseId = "#caseId"
        )
        public void deleteCpos_LogSingleIdsAndListOfIds(List<String> cpoIds,
                                                        List<String> caseIds,
                                                        String cpoId,
                                                        String caseId) {
        }

        @LogAudit(
            operationType = AuditOperationType.DELETE_CASE_PAYMENT_ORDER,
            cpoIds = "#cpoIds",
            caseIds = "#caseIds"
        )
        public void deleteCpos_ThrowError(List<String> cpoIds,
                                          List<String> caseIds) {
            throw new RuntimeException();
        }

        @SuppressWarnings("UnusedReturnValue")
        @LogAudit(
            operationType = AuditOperationType.UPDATE_CASE_PAYMENT_ORDER,
            cpoId = "#updateRequest.id",
            caseId = "#updateRequest.caseId",
            cpoIds = "#bad-path"
        )
        public CasePaymentOrder updateCpo_LogUsingBadPath(UpdateCasePaymentOrderRequest updateRequest) {
            return CasePaymentOrder.builder()
                .id(UUID.fromString(CPO_ID_VALID_1))
                .caseId(Long.parseLong(CASE_ID_VALID_1))
                .build();
        }
    }
}
