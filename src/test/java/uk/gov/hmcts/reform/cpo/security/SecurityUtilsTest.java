package uk.gov.hmcts.reform.cpo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.BaseTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.cpo.security.Permission.CREATE;
import static uk.gov.hmcts.reform.cpo.security.Permission.DELETE;
import static uk.gov.hmcts.reform.cpo.security.Permission.READ;
import static uk.gov.hmcts.reform.cpo.security.Permission.UPDATE;
import static uk.gov.hmcts.reform.cpo.security.SecurityUtils.SERVICE_AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest implements BaseTest {

    private static final String XUI_WEBAPP_SERVICE_NAME = "XUI_WEBAPP";

    @InjectMocks
    private SecurityUtils securityUtils;

    @Mock
    private IdamRepository idamRepository;

    @Mock
    private ServiceAuthorizationConfig serviceAuthorizationConfig;

    @Captor
    private ArgumentCaptor<String> serviceNameArgumentCaptor;

    @Captor
    private ArgumentCaptor<Permission> permissionArgumentCaptor;

    @BeforeEach
    void setUp() {
        securityUtils = new SecurityUtils(idamRepository, serviceAuthorizationConfig);
    }

    private void setS2SToken(String serviceName) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (serviceName != null) {
            request.addHeader(SERVICE_AUTHORIZATION, generateDummyS2SToken(serviceName));
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("Should get service name from token")
    void shouldGetServiceNameFromToken() {
        // GIVEN
        String serviceName = "ServiceName";
        String s2SToken = generateDummyS2SToken(serviceName);

        // WHEN
        String response = securityUtils.getServiceNameFromS2SToken(s2SToken);

        // THEN
        assertEquals(serviceName, response, "Service name should match value from token:");
    }

    @Test
    @DisplayName("Should get service name from token when BEARER is also supplied")
    void shouldGetServiceNameFromTokenWithBearer() {
        // GIVEN
        String serviceName = "ServiceName";
        String s2SToken = SecurityUtils.BEARER + generateDummyS2SToken(serviceName);

        // WHEN
        String response = securityUtils.getServiceNameFromS2SToken(s2SToken);

        // THEN
        assertEquals(serviceName, response, "Service name should match value from token:");
    }

    @Test
    @DisplayName("Should get blank service name from blank token")
    void shouldGetBlankServiceNameFromBlankToken() {
        // WHEN
        String response = securityUtils.getServiceNameFromS2SToken(null);

        // THEN
        assertNull(response, "Service name should match value from token:");
    }

    @Test
    void testHasCreateAccessReturnsFalseWhenServiceAuthorizationHeaderIsNull() {
        setS2SToken(null);
        assertFalse(securityUtils.hasCreatePermission());
    }

    @Test
    void testHasCreateAccessReturnsFalseWhenRequestAttributesAreNull() {
        setS2SToken(null);
        RequestContextHolder.resetRequestAttributes();
        assertFalse(securityUtils.hasCreatePermission());
    }

    @Test
    void testHasCreateAccessReturnsTrueWhenCalledByAppWithCreatePermissions() {
        assertParametersSuppliedToServiceAuthorizatrionConfig(CREATE);
    }

    @Test
    void testHasReadAccessReturnsTrueWhenCalledByAppWithReadPermissions() {
        assertParametersSuppliedToServiceAuthorizatrionConfig(READ);
    }

    @Test
    void testHasUpdateAccessReturnsTrueWhenCalledByAppWithUpdatePermissions() {
        assertParametersSuppliedToServiceAuthorizatrionConfig(UPDATE);
    }

    @Test
    void testHasDeleteAccessReturnsTrueWhenCalledByAppWithDeletePermissions() {
        assertParametersSuppliedToServiceAuthorizatrionConfig(DELETE);
    }

    void assertParametersSuppliedToServiceAuthorizatrionConfig(Permission permission) {
        setS2SToken(XUI_WEBAPP_SERVICE_NAME);
        callMethodUnderTest(permission);
        verify(serviceAuthorizationConfig).hasPermissions(serviceNameArgumentCaptor.capture(),
                permissionArgumentCaptor.capture());
        assertEquals(XUI_WEBAPP_SERVICE_NAME, serviceNameArgumentCaptor.getValue());
        assertEquals(permission, permissionArgumentCaptor.getValue());
    }

    private void callMethodUnderTest(Permission permission) {
        switch (permission) {
            case CREATE:
                securityUtils.hasCreatePermission();
                break;
            case READ:
                securityUtils.hasReadPermission();
                break;
            case UPDATE:
                securityUtils.hasUpdatePermission();
                break;
            case DELETE:
                securityUtils.hasDeletePermission();
                break;
        }
    }

    private static String generateDummyS2SToken(String serviceName) {
        return Jwts.builder()
                .setSubject(serviceName)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
                .compact();
    }

}
