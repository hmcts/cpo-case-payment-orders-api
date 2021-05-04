package uk.gov.hmcts.reform.cpo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.BaseTest;

import java.util.Date;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNull;

class SecurityUtilsTest implements BaseTest {

    @InjectMocks
    SecurityUtils securityUtils;

    @Mock
    IdamRepository idamRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        assertEquals("Service name should match value from token:",
                     serviceName, response);
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
        assertEquals("Service name should match value from token:",
                     serviceName, response);
    }

    @Test
    @DisplayName("Should get blank service name from blank token")
    void shouldGetBlankServiceNameFromBlankToken() {
        // WHEN
        String response = securityUtils.getServiceNameFromS2SToken(null);

        // THEN
        assertNull("Service name should match value from token:",  response);
    }


    private static String generateDummyS2SToken(String serviceName) {
        return Jwts.builder()
            .setSubject(serviceName)
            .setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
            .compact();
    }

}
