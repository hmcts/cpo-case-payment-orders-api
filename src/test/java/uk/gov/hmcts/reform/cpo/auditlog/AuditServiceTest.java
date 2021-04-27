package uk.gov.hmcts.reform.cpo.auditlog;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.auditlog.aop.AuditContext;
import uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

class AuditServiceTest implements BaseTest {

    private static final String INVOKING_SERVICE = "Test Invoking Service";

    @InjectMocks
    private AuditService auditService;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Captor
    ArgumentCaptor<AuditEntry> captor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        given(securityUtils.getServiceNameFromS2SToken(anyString())).willReturn(INVOKING_SERVICE);
    }

    @Test
    @DisplayName("should save to audit repository")
    void shouldSaveToAuditRepository() {

        // GIVEN
        AuditContext auditContext = AuditContext.auditContextWith()
            .auditOperationType(AuditOperationType.CREATE_CASE_PAYMENT_ORDER)
            .invokingService(INVOKING_SERVICE)
            .httpMethod(HttpMethod.GET.name())
            .httpStatus(HttpStatus.OK.value())
            .requestPath(CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH)
            .requestId(REQUEST_ID)
            .build();

        // WHEN
        auditService.audit(auditContext);

        // THEN
        verify(auditRepository).save(captor.capture());

        AuditEntry auditEntry = captor.getValue();
        assertNotNull("Should save with date and time", auditEntry.getDateTime());


        assertEquals("Should save with Operation Type",
                     auditContext.getAuditOperationType().getLabel(), auditEntry.getOperationType());
        assertEquals("Should save with Invoking Service",
                     auditContext.getInvokingService(), auditEntry.getInvokingService());
        assertEquals("Should save with HTTP Method",
                     auditContext.getHttpMethod(), auditEntry.getHttpMethod());
        assertEquals("Should save with HTTP Status",
                     auditContext.getHttpStatus(), auditEntry.getHttpStatus());
        assertEquals("Should save with Request Path",
                     auditContext.getRequestPath(), auditEntry.getRequestPath());
        assertEquals("Should save with Request ID",
                     auditContext.getRequestId(), auditEntry.getRequestId());

    }

    @Test
    @DisplayName("should still save to audit repository when null OperationType")
    void shouldSaveToAuditRepositoryWhenOperationTypeIsNull() {

        // GIVEN
        AuditContext auditContext = AuditContext.auditContextWith()
            .auditOperationType(null)
            .httpStatus(HttpStatus.OK.value())
            .build();

        // WHEN
        auditService.audit(auditContext);

        // THEN
        verify(auditRepository).save(captor.capture());

        AuditEntry auditEntry = captor.getValue();
        assertEquals("Should save with HTTP Status",
                     auditContext.getHttpStatus(), auditEntry.getHttpStatus());
        assertNull("Should save with null Operation Type",
                   auditEntry.getOperationType());

    }

    @Test
    @DisplayName("should save to audit repository with IDAM/User ID")
    void shouldSaveToAuditRepositoryWithIdamId() {

        // GIVEN
        AuditContext auditContext = AuditContext.auditContextWith()
            .httpStatus(HttpStatus.OK.value())
            .build();

        UserInfo userInfo = UserInfo.builder()
            .uid(CREATED_BY)
            .build();
        given(securityUtils.getUserInfo()).willReturn(userInfo);

        // WHEN
        auditService.audit(auditContext);

        // THEN
        verify(auditRepository).save(captor.capture());

        AuditEntry auditEntry = captor.getValue();
        assertEquals("Should save with HTTP Status",
                     auditContext.getHttpStatus(), auditEntry.getHttpStatus());
        assertEquals("Should save with IDAM/User ID",
                     userInfo.getUid(), auditEntry.getIdamId());

    }

    @Test
    @DisplayName("should save to audit repository when single IDs")
    void shouldSaveToAuditRepositoryWhenSingleIds() {

        // GIVEN
        AuditContext auditContext = AuditContext.auditContextWith()
            .cpoId(CPO_ID_VALID_1)
            .caseId(CASE_ID_VALID_1)
            .build();

        // WHEN
        auditService.audit(auditContext);

        // THEN
        verify(auditRepository).save(captor.capture());

        AuditEntry auditEntry = captor.getValue();
        assertTrue("Should save with CPO ID", auditEntry.getCpoIds().contains(auditContext.getCpoId()));
        assertTrue("Should save with Case ID", auditEntry.getCaseIds().contains(auditContext.getCaseId()));

    }

    @Test
    @DisplayName("should save to audit repository when list of IDs")
    void shouldSaveToAuditRepositoryWhenListOfIds() {

        // GIVEN
        List<String> cpoIds = List.of(CPO_ID_VALID_1, CPO_ID_VALID_2);
        List<String> caseIds = List.of(CASE_ID_VALID_1, CASE_ID_VALID_2);
        AuditContext auditContext = AuditContext.auditContextWith()
            .cpoIds(cpoIds)
            .caseIds(caseIds)
            .build();

        // WHEN
        auditService.audit(auditContext);

        // THEN
        verify(auditRepository).save(captor.capture());

        AuditEntry auditEntry = captor.getValue();
        assertTrue("Should save with list of CPO IDs", auditEntry.getCpoIds().containsAll(cpoIds));
        assertTrue("Should save with list of Case IDs", auditEntry.getCaseIds().containsAll(caseIds));

    }

    @Test
    @DisplayName("should save to audit repository when single and list of IDs")
    void shouldSaveToAuditRepositoryWhenSingleAndListOfIds() {

        // GIVEN
        List<String> cpoIds = Lists.newArrayList(CPO_ID_VALID_1, CPO_ID_VALID_2);
        List<String> caseIds = Lists.newArrayList(CASE_ID_VALID_1, CASE_ID_VALID_2);
        String cpoId = CPO_ID_VALID_3;
        String caseId = CASE_ID_VALID_3;
        AuditContext auditContext = AuditContext.auditContextWith()
            .cpoIds(cpoIds)
            .caseIds(caseIds)
            .cpoId(cpoId)
            .caseId(caseId)
            .build();

        // WHEN
        auditService.audit(auditContext);

        // THEN
        verify(auditRepository).save(captor.capture());

        AuditEntry auditEntry = captor.getValue();
        assertTrue("Should save with list of CPO ID", auditEntry.getCpoIds().containsAll(cpoIds));
        assertTrue("Should save with list of Case ID", auditEntry.getCaseIds().containsAll(caseIds));
        assertTrue("Should save with single CPO ID", auditEntry.getCpoIds().contains(cpoId));
        assertTrue("Should save with single Case ID", auditEntry.getCaseIds().contains(caseId));

    }

    @Test
    @DisplayName("should load invoking service from S2S token")
    void shouldLoadInvokingServiceFromS2SToken() {

        // GIVEN
        String s2sToken = "Test Token";
        String expectedInvokingServiceName = "Test InvokingService";
        MockHttpServletRequest request =
            new MockHttpServletRequest(HttpMethod.GET.name(), CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH);
        request.addHeader(SecurityUtils.SERVICE_AUTHORIZATION, s2sToken);
        given(securityUtils.getServiceNameFromS2SToken(anyString())).willReturn(expectedInvokingServiceName);

        // WHEN
        String response = auditService.getInvokingService(request);

        // THEN
        verify(securityUtils).getServiceNameFromS2SToken(s2sToken);
        assertEquals("Should return service name from securityUtils", expectedInvokingServiceName, response);

    }


}
