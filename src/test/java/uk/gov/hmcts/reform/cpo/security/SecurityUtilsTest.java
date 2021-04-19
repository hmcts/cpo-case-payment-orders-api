package uk.gov.hmcts.reform.cpo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static io.jsonwebtoken.impl.TextCodec.BASE64;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

    @InjectMocks
    private SecurityUtils securityUtils;
    @Mock
    private IdamRepository idamRepository;

    private static final String READ_ONLY_SERVICE = "read_only_service";
    private static final String FULL_ACCESS_SERVICE = "full_access_service";

    @BeforeEach
    public void setUp() {
        securityUtils = new SecurityUtils(idamRepository, asList(READ_ONLY_SERVICE));
    }

    @Test
    public void hasFullAccessToServiceShouldReturnTrue() {
        assertTrue("hasFullAccessToService should return t when token contains service name with full CRUD access",
            securityUtils.hasFullAccessToService(generateDummyS2SToken(FULL_ACCESS_SERVICE)));
    }

    @Test
    public void hasFullAccessToServiceShouldReturnFalse() {
        assertFalse("hasFullAccessToService should return false when token contains name of a read only service",
                securityUtils.hasFullAccessToService(generateDummyS2SToken(READ_ONLY_SERVICE)));
    }

    @Test
    public void hasFullAccessToServiceReturnsFalseWhenSuppliedWithEmptyString() {
        assertFalse("hasFullAccessToService should return false given an empty token string",
                securityUtils.hasFullAccessToService(""));
    }

    @Test
    public void hasFullAccessToServiceReturnsFalseWhenSuppliedWithNull() {
        assertFalse("hasFullAccessToService should return false given an empty token string",
                securityUtils.hasFullAccessToService(null));
    }

    private static String generateDummyS2SToken(String serviceName) {
        return Jwts.builder()
            .setSubject(serviceName)
            .setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, BASE64.encode("AA"))
            .compact();
    }
}
