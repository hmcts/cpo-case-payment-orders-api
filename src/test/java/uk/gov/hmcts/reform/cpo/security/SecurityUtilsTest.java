package uk.gov.hmcts.reform.cpo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.cpo.exception.UnauthorisedServiceException;

import java.util.Date;

import static io.jsonwebtoken.impl.TextCodec.BASE64;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

public class SecurityUtilsTest {

    @InjectMocks
    private SecurityUtils securityUtils;
    @Mock
    private IdamRepository idamRepository;

    private static final String READ_ONLY_SERVICE = "read_only_service";


    @BeforeEach
    public void setUp() {
        openMocks(this);
        securityUtils = new SecurityUtils(idamRepository, asList(READ_ONLY_SERVICE));
    }

    @Test
    public void isReadOnlyServiceShouldThrowException() {
        assertThrows(UnauthorisedServiceException.class, () ->
            securityUtils.isReadOnlyService(generateDummyS2SToken(READ_ONLY_SERVICE)));
    }

    @Test
    public void isReadOnlyServiceShouldThrowExceptionWhenEmptyStringPassed() {
        assertThrows(UnauthorisedServiceException.class, () -> securityUtils.isReadOnlyService(""));
    }

    @Test
    public void isReadOnlyServiceShouldThrowExceptionWhenNullPassed() {
        assertThrows(UnauthorisedServiceException.class, () -> securityUtils.isReadOnlyService(null));
    }

    private static String generateDummyS2SToken(String serviceName) {
        return Jwts.builder()
            .setSubject(serviceName)
            .setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, BASE64.encode("AA"))
            .compact();
    }
}
