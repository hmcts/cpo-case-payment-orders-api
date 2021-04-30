package uk.gov.hmcts.reform.cpo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.reform.TestIdamConfiguration;
import uk.gov.hmcts.reform.cpo.auditlog.AuditEntry;
import uk.gov.hmcts.reform.cpo.auditlog.AuditOperationType;
import uk.gov.hmcts.reform.cpo.auditlog.AuditRepository;
import uk.gov.hmcts.reform.cpo.config.AuditConfiguration;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static uk.gov.hmcts.reform.cpo.security.SecurityUtils.SERVICE_AUTHORIZATION;

@SpringBootTest(classes = {
    Application.class,
    TestIdamConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
@SuppressWarnings({"squid:S2187"})
public class BaseTest {

    public static final String AUTHORISED_CRUD_SERVICE = "TEST_CRUD_SERVICE";
    public static final String AUTHORISED_READ_SERVICE = "TEST_READ_SERVICE";

    protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final String ERROR_PATH_DETAILS = "$.details";
    public static final String ERROR_PATH_ERROR = "$.error";
    public static final String ERROR_PATH_MESSAGE = "$.message";
    public static final String ERROR_PATH_STATUS = "$.status";

    public static final String CASE_ID_INVALID = "INVALID_NON_NUMERIC";

    public static final String CPO_ID_VALID_1 = "df54651b-3227-4067-9f23-6ffb32e2c6bd";
    public static final String CPO_ID_INVALID = "INVALID_NON_NUMERIC";

    public static final String ORDER_REFERENCE_VALID = "2021-11223344556";
    public static final String ORDER_REFERENCE_INVALID = "2021-918425346";
    public static final String ACTION = "action";
    public static final String RESPONSIBLE_PARTY = "responsibleParty";
    public static final LocalDateTime EFFECTIVE_FROM = LocalDateTime.of(2021, Month.MARCH, 24, 11, 48, 32);
    public static final boolean HISTORY_EXISTS_DEFAULT = false;
    public static final boolean HISTORY_EXISTS_UPDATED = true;

    public static final String IDAM_MOCK_USER_ID = "e8275d41-7f22-4ee7-8ed3-14644d6db096";

    private static final String EXAMPLE_REQUEST_ID = "TEST REQUEST ID";

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;

    @SpyBean
    @Inject
    protected AuditRepository auditRepository;

    @SuppressWarnings("squid:S5979")
    @Mock
    protected Authentication authentication;

    @BeforeEach
    void init() {
        Jwt jwt = generateDummyAuthToken();
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    protected HttpHeaders createHttpHeaders(String serviceName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer EncodedAuthToken");
        String s2SToken = generateDummyS2SToken(serviceName);
        headers.add(SERVICE_AUTHORIZATION, s2SToken);
        headers.add(AuditConfiguration.REQUEST_ID, EXAMPLE_REQUEST_ID);
        return headers;
    }

    protected ResultMatcher hasGeneratedLogAudit(AuditOperationType operationType,
                                                 String invokingService,
                                                 String cpoId,
                                                 String caseId) {
        return result -> verifyLogAuditValues(result,
                                              operationType,
                                              invokingService,
                                              cpoId,
                                              caseId);
    }

    protected ResultMatcher hasGeneratedLogAudit(AuditOperationType operationType,
                                                 String invokingService,
                                                 List<String> cpoIds,
                                                 List<String> caseIds) {
        return result -> verifyLogAuditValues(result,
                                              operationType,
                                              invokingService,
                                              cpoIds,
                                              caseIds);
    }

    protected void verifyLogAuditValues(MvcResult result,
                                        AuditOperationType operationType,
                                        String invokingService,
                                        String cpoId,
                                        String caseId) {
        verifyLogAuditValues(result,
                             operationType,
                             invokingService,
                             StringUtils.isNoneBlank(cpoId) ? List.of(cpoId) : new ArrayList<>(),
                             StringUtils.isNoneBlank(caseId) ? List.of(caseId) : new ArrayList<>());
    }

    protected void verifyLogAuditValues(MvcResult result,
                                        AuditOperationType operationType,
                                        String invokingService,
                                        List<String> cpoIds,
                                        List<String> caseIds) {
        ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditRepository).save(captor.capture());

        AuditEntry auditEntry = captor.getValue();

        assertNotNull("DateTime", auditEntry.getDateTime());

        assertEquals("Operation Type", operationType.getLabel(), auditEntry.getOperationType());

        assertEquals("Idam ID", IDAM_MOCK_USER_ID, auditEntry.getIdamId());
        assertEquals("Invoking Service", invokingService, auditEntry.getInvokingService());

        assertEquals("HTTP Status", result.getResponse().getStatus(), auditEntry.getHttpStatus());
        assertEquals("HTTP Method", result.getRequest().getMethod(), auditEntry.getHttpMethod());
        assertEquals("Request Path", result.getRequest().getRequestURI(), auditEntry.getRequestPath());
        assertEquals("Request ID", EXAMPLE_REQUEST_ID, auditEntry.getRequestId());

        // NB: skip validation of inputs for BAD_REQUEST as some may not have been populated
        if (result.getResponse().getStatus() != HttpStatus.BAD_REQUEST.value()) {
            if (cpoIds != null && !cpoIds.isEmpty()) {
                assertThat(auditEntry.getCpoIds())
                    .isNotNull()
                    .hasSize(cpoIds.size())
                    .containsAll(cpoIds);
            } else {
                assertThat(auditEntry.getCpoIds()).isNullOrEmpty();
            }

            if (caseIds != null && !caseIds.isEmpty()) {
                assertThat(auditEntry.getCaseIds())
                    .isNotNull()
                    .hasSize(caseIds.size())
                    .containsAll(caseIds);
            } else {
                assertThat(auditEntry.getCaseIds()).isNullOrEmpty();
            }
        }
    }

    private Jwt generateDummyAuthToken() {
        return Jwt.withTokenValue("a dummy auth token")
            .claim("aClaim", "aClaim")
            .header("aHeader", "aHeader")
            .build();
    }

    private static String generateDummyS2SToken(String serviceName) {
        return Jwts.builder()
            .setSubject(serviceName)
            .setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
            .compact();
    }

}
