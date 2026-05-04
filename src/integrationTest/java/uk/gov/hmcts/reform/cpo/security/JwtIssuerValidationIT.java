package uk.gov.hmcts.reform.cpo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersAuditJpaRepository;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersJpaRepository;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;

class JwtIssuerValidationIT extends BaseTest {

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
    void shouldRejectJwtFromUnexpectedIssuer() throws Exception {
        HttpHeaders headers = createHttpHeaders(AUTH_TOKEN_TTL,
                                                AUTHORISED_CREATE_SERVICE,
                                                AUTH_TOKEN_TTL,
                                                UNEXPECTED_ISSUER);

        mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                            .headers(headers)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createRequestBody()))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, org.hamcrest.Matchers.startsWith("Bearer")));
    }

    private String createRequestBody() throws com.fasterxml.jackson.core.JsonProcessingException {
        CreateCasePaymentOrderRequest request = new CreateCasePaymentOrderRequest(
            entityGenerator.generateUniqueCaseId(),
            ACTION,
            RESPONSIBLE_PARTY,
            ORDER_REFERENCE_VALID
        );
        return objectMapper.writeValueAsString(request);
    }
}
