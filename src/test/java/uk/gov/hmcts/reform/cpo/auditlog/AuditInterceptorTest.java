package uk.gov.hmcts.reform.cpo.auditlog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.auditlog.aop.AuditContext;
import uk.gov.hmcts.reform.cpo.auditlog.aop.AuditContextHolder;
import uk.gov.hmcts.reform.cpo.config.AuditConfiguration;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNull;

class AuditInterceptorTest implements BaseTest {

    private static final int STATUS_HIDDEN = HttpStatus.IM_A_TEAPOT_418;
    private static final int STATUS_NOT_HIDDEN = HttpStatus.OK_200;
    private static final String METHOD = "GET";
    private static final String REQUEST_URI = "/Test-URI";

    private AuditContext auditContext;

    @InjectMocks
    private AuditInterceptor interceptor;

    @Mock
    private AuditConfiguration auditConfiguration;

    @Mock
    private AuditService auditService;

    @Mock
    private HandlerMethod handler;

    @Mock
    private LogAudit logAudit;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new MockHttpServletRequest(METHOD, REQUEST_URI);
        request.addHeader(AuditConfiguration.REQUEST_ID, REQUEST_ID);
        response = new MockHttpServletResponse();
        response.setStatus(STATUS_NOT_HIDDEN);

        given(auditConfiguration.isAuditLogEnabled()).willReturn(true);
        given(auditConfiguration.isHttpStatusIgnored(STATUS_HIDDEN)).willReturn(true);
        given(auditConfiguration.isHttpStatusIgnored(STATUS_NOT_HIDDEN)).willReturn(false);

        auditContext = new AuditContext();
        AuditContextHolder.setAuditContext(auditContext);
    }

    @Test
    @DisplayName("Should prepare audit context with HTTP semantics")
    void shouldPrepareAuditContextWithHttpSemantics() {

        // GIVEN
        given(handler.hasMethodAnnotation(LogAudit.class)).willReturn(true);

        // WHEN
        interceptor.afterCompletion(request, response, handler, null);

        // THEN
        assertEquals("Should populate HTTP Status", response.getStatus(), auditContext.getHttpStatus());
        assertEquals("Should populate HTTP Status", METHOD, auditContext.getHttpMethod());
        assertEquals("Should populate HTTP Status", REQUEST_URI, auditContext.getRequestPath());
        assertEquals("Should populate HTTP Status", REQUEST_ID, auditContext.getRequestId());

        verify(auditService).audit(auditContext);
    }

    @Test
    @DisplayName("Should prepare audit context with InvokingService")
    void shouldPrepareAuditContextWithInvokingService() {

        // GIVEN
        given(handler.hasMethodAnnotation(LogAudit.class)).willReturn(true);
        String testInvokingServiceName = "Test InvokingService";
        given(auditService.getInvokingService(any())).willReturn(testInvokingServiceName);

        // WHEN
        interceptor.afterCompletion(request, response, handler, null);

        // THEN
        assertEquals("Should populate InvokingService",
                     testInvokingServiceName, auditContext.getInvokingService());

        verify(auditService).audit(auditContext);
    }

    @Test
    @DisplayName("Should prepare audit context with OperationType")
    void shouldPrepareAuditContextWithOperationType() {

        // GIVEN
        AuditOperationType operationType = AuditOperationType.CREATE_CASE_PAYMENT_ORDER;
        given(handler.hasMethodAnnotation(LogAudit.class)).willReturn(true);
        given(handler.getMethodAnnotation(LogAudit.class)).willReturn(logAudit);
        given(logAudit.operationType()).willReturn(operationType);


        // WHEN
        interceptor.afterCompletion(request, response, handler, null);

        // THEN
        assertEquals("Should populate OperationType",
                     AuditOperationType.CREATE_CASE_PAYMENT_ORDER, auditContext.getAuditOperationType());

        verify(auditService).audit(auditContext);
    }

    @Test
    @DisplayName("Should not audit when annotation is not present")
    void shouldNotAuditForWhenAnnotationIsNotPresent() {

        // GIVEN
        given(handler.hasMethodAnnotation(LogAudit.class)).willReturn(false);

        // WHEN
        interceptor.afterCompletion(request, response, handler, null);

        // THEN
        verifyNoMoreInteractions(auditService);
    }

    @Test
    @DisplayName("Should not audit when status is hidden from audit")
    void shouldNotAuditWhenStatusIsHidden() {

        // GIVEN
        response.setStatus(STATUS_HIDDEN);
        given(handler.hasMethodAnnotation(LogAudit.class)).willReturn(true);

        // WHEN
        interceptor.afterCompletion(request, response, handler, null);

        // THEN
        verifyNoMoreInteractions(auditService);
    }

    @Test
    @DisplayName("Should always clear audit context")
    void shouldClearAuditContextAlways() {

        // GIVEN
        given(handler.hasMethodAnnotation(LogAudit.class)).willReturn(true);
        doThrow(new RuntimeException("audit failure")).when(auditService).audit(auditContext);

        // WHEN
        interceptor.afterCompletion(request, response, handler, null);

        // THEN
        assertNull("Should clear audit context on completion", AuditContextHolder.getAuditContext());
    }

    @Test
    @DisplayName("Should not audit if disabled")
    void shouldNotAuditIfDisabled() {

        // GIVEN
        given(auditConfiguration.isAuditLogEnabled()).willReturn(false);

        // WHEN
        interceptor.afterCompletion(request, response, handler, null);

        // THEN
        verifyNoMoreInteractions(auditService);
    }

    @Test
    @DisplayName("Should not audit if bad handler")
    void shouldNotAuditIfBadHandler() {

        // WHEN
        interceptor.afterCompletion(request, response, new Object(), null);

        // THEN
        verifyNoMoreInteractions(auditService);
    }

}
