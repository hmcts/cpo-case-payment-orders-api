package uk.gov.hmcts.reform.cpo;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.TestIdamConfiguration;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    Application.class,
    TestIdamConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
@SuppressWarnings({"squid:S2187"})
public class BaseTest {

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

    public static final String CREATED_BY_IDAM_MOCK = "e8275d41-7f22-4ee7-8ed3-14644d6db096";

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;

    @SuppressWarnings("squid:S5979")
    @Mock
    protected Authentication authentication;

    @BeforeEach
    void init() {
        Jwt jwt = dummyJwt();
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    private Jwt dummyJwt() {
        return Jwt.withTokenValue("a dummy jwt token")
            .claim("aClaim", "aClaim")
            .header("aHeader", "aHeader")
            .build();
    }

}
