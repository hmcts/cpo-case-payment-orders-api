package uk.gov.hmcts.reform.cpo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Date;

import static io.jsonwebtoken.impl.TextCodec.BASE64;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.cpo.security.Permission.CREATE;
import static uk.gov.hmcts.reform.cpo.security.Permission.DELETE;
import static uk.gov.hmcts.reform.cpo.security.Permission.READ;
import static uk.gov.hmcts.reform.cpo.security.Permission.UPDATE;
import static uk.gov.hmcts.reform.cpo.security.SecurityUtils.SERVICE_AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

    private  static final String XUI_WEBAPP_SERVICE_NAME = "XUI_WEBAPP";

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
    void testHasCreateAccessReturnsFalseWhenServiceAuthorizationHeaderIsNull() {
        setS2SToken(null);
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
            .signWith(SignatureAlgorithm.HS256, BASE64.encode("AA"))
            .compact();
    }
}
