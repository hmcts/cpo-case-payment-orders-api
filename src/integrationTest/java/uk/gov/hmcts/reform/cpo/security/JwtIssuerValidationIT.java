package uk.gov.hmcts.reform.cpo.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersAuditJpaRepository;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersJpaRepository;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;

@TestPropertySource(properties = "oidc.allowed-issuers= http://confirmed-secondary-issuer ")
class JwtIssuerValidationIT extends BaseTest {

    private static final String CONFIRMED_SECONDARY_ISSUER = "http://confirmed-secondary-issuer";
    private static final String UNEXPECTED_ISSUER = "http://unexpected-issuer";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CasePaymentOrderEntityGenerator entityGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CasePaymentOrdersJpaRepository casePaymentOrdersJpaRepository;

    @Autowired
    private CasePaymentOrdersAuditJpaRepository casePaymentOrdersAuditJpaRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        casePaymentOrdersJpaRepository.deleteAllInBatch();
        casePaymentOrdersAuditJpaRepository.deleteAllInBatch();
    }

    @Test
    void shouldAcceptRequestWhenJwtIssuerMatchesTrimmedAllowedIssuer() {
        assertPostCasePaymentOrder(CONFIRMED_SECONDARY_ISSUER, status().isCreated());
    }

    @Test
    void shouldRejectRequestWhenJwtIssuerIsUnexpected() {
        assertPostCasePaymentOrder(UNEXPECTED_ISSUER,
                                   status().isUnauthorized(),
                                   header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")));
    }

    private void assertPostCasePaymentOrder(String issuer, ResultMatcher... resultMatchers) {
        HttpHeaders headers = createHeaders(issuer);
        try {
            ResultActions resultActions = mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                                                              .headers(headers)
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content(createRequestBody()));
            for (ResultMatcher resultMatcher : resultMatchers) {
                resultActions.andExpect(resultMatcher);
            }
        } catch (Exception e) {
            fail("Failed to assert case payment order response", e);
        }
    }

    private HttpHeaders createHeaders(String issuer) {
        try {
            return createHttpHeaders(AUTH_TOKEN_TTL,
                                     AUTHORISED_CREATE_SERVICE,
                                     AUTH_TOKEN_TTL,
                                     issuer);
        } catch (JOSEException e) {
            return fail("Failed to create auth headers", e);
        }
    }

    private String createRequestBody() throws JsonProcessingException {
        CreateCasePaymentOrderRequest request = new CreateCasePaymentOrderRequest(
            entityGenerator.generateUniqueCaseId(),
            ACTION,
            RESPONSIBLE_PARTY,
            ORDER_REFERENCE_VALID
        );
        return objectMapper.writeValueAsString(request);
    }
}
