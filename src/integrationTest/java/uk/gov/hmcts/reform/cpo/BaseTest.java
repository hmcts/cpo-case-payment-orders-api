package uk.gov.hmcts.reform.cpo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.springframework.http.HttpStatus;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import jakarta.inject.Inject;
import uk.gov.hmcts.reform.TestIdamConfiguration;
import uk.gov.hmcts.reform.cpo.auditlog.AuditEntry;
import uk.gov.hmcts.reform.cpo.auditlog.AuditOperationType;
import uk.gov.hmcts.reform.cpo.auditlog.AuditRepository;
import uk.gov.hmcts.reform.cpo.config.AuditConfiguration;
import static uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter.TOKEN_NAME;
import static uk.gov.hmcts.reform.cpo.security.SecurityUtils.BEARER;
import static uk.gov.hmcts.reform.cpo.security.SecurityUtils.SERVICE_AUTHORIZATION;
import uk.gov.hmcts.reform.cpo.utils.KeyGenUtil;

@SpringBootTest(classes = {
    Application.class,
    TestIdamConfiguration.class
})
@AutoConfigureMockMvc() // NB: don't disable filters as they are needed to test authentication is enabled on endpoints
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
@SuppressWarnings({"squid:S2187"})
public class BaseTest {

    public static final String AUTHORISED_CRUD_SERVICE = "test_crud_service";

    public static final String AUTHORISED_CREATE_SERVICE = "test_create_service";
    public static final String AUTHORISED_READ_SERVICE = "test_read_service";
    public static final String AUTHORISED_UPDATE_SERVICE = "test_update_service";
    public static final String AUTHORISED_DELETE_SERVICE = "test_delete_service";

    public static final String UNAUTHORISED_SERVICE = "test_unauthorised_service";

    public static final long AUTH_TOKEN_TTL = 14400000;

    public static final String ERROR_PATH_DETAILS = "$.details";
    public static final String ERROR_PATH_ERROR = "$.error";
    public static final String ERROR_PATH_MESSAGE = "$.message";
    public static final String ERROR_PATH_STATUS = "$.status";

    public static final String CASE_ID_INVALID = "INVALID_NON_NUMERIC";

    public static final String CPO_ID_VALID_1 = "df54651b-3227-4067-9f23-6ffb32e2c6bd";
    public static final String CPO_ID_INVALID = "INVALID_NON_NUMERIC";

    public static final String ORDER_REFERENCE_VALID = "2021-1122334455667";
    public static final String ORDER_REFERENCE_INVALID = "2021-918425346";
    public static final String ACTION = "action";
    public static final String RESPONSIBLE_PARTY = "responsibleParty";
    public static final boolean HISTORY_EXISTS_DEFAULT = false;
    public static final boolean HISTORY_EXISTS_UPDATED = true;

    public static final String IDAM_MOCK_USER_ID = "e8275d41-7f22-4ee7-8ed3-14644d6db096";

    private static final String EXAMPLE_REQUEST_ID = "TEST REQUEST ID";

    @MockitoSpyBean
    @Inject
    protected AuditRepository auditRepository;

    public static HttpHeaders createHttpHeaders(String serviceName) throws JOSEException {
        return createHttpHeaders(AUTH_TOKEN_TTL, serviceName, AUTH_TOKEN_TTL);
    }

    public static HttpHeaders createHttpHeaders(long authTtlMillis,
                                                String serviceName,
                                                long s2sAuthTtlMillis) throws JOSEException {
        HttpHeaders headers = new HttpHeaders();
        // :: IDAM OAuth2 token
        String authToken = BEARER + generateAuthToken(authTtlMillis);
        headers.add(AUTHORIZATION, authToken);
        // :: S2S authentication token
        String s2SToken = generateS2SToken(serviceName, s2sAuthTtlMillis);
        headers.add(SERVICE_AUTHORIZATION, s2SToken);
        // :: LogAudit Request-ID header
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

    private static String generateAuthToken(long ttlMillis) throws JOSEException  {

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
            .subject("CPO_Stub")
            .issueTime(new Date())
            .claim(TOKEN_NAME, ACCESS_TOKEN)
            .expirationTime(new Date(System.currentTimeMillis() + ttlMillis));

        SignedJWT signedJWT = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(KeyGenUtil.getRsaJWK().getKeyID()).build(),
            builder.build()
        );
        signedJWT.sign(new RSASSASigner(KeyGenUtil.getRsaJWK()));

        return signedJWT.serialize();
    }

    private static String generateS2SToken(String serviceName, long ttlMillis) {
        return Jwts.builder()
            .setSubject(serviceName)
            .setExpiration(new Date(System.currentTimeMillis() + ttlMillis))
            .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
            .compact();
    }

}
