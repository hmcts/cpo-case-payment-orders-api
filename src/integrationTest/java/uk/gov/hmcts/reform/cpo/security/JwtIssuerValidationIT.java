package uk.gov.hmcts.reform.cpo.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersAuditJpaRepository;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersJpaRepository;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;
import uk.gov.hmcts.reform.cpo.utils.KeyGenUtil;

import java.time.Instant;
import java.util.Date;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;

@TestPropertySource(properties = "oidc.allowed-issuers= http://confirmed-secondary-issuer ")
class JwtIssuerValidationIT extends BaseTest {

    private static final String CONFIRMED_SECONDARY_ISSUER = "http://confirmed-secondary-issuer";
    private static final String UNEXPECTED_ISSUER = "http://unexpected-issuer";
    private static final Instant TOKEN_ISSUED_AT = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant TOKEN_EXPIRES_AT = Instant.parse("2099-01-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CasePaymentOrderEntityGenerator entityGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtDecoder jwtDecoder;

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
    void shouldAcceptRequestWhenJwtIssuerMatchesTrimmedAllowedIssuer() throws Exception {
        HttpHeaders headers = createHttpHeaders(AUTH_TOKEN_TTL,
                                                AUTHORISED_CREATE_SERVICE,
                                                AUTH_TOKEN_TTL,
                                                CONFIRMED_SECONDARY_ISSUER);

        mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                            .headers(headers)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createRequestBody()))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectRequestWhenJwtIssuerIsUnexpected() throws Exception {
        HttpHeaders headers = createHttpHeaders(AUTH_TOKEN_TTL,
                                                AUTHORISED_CREATE_SERVICE,
                                                AUTH_TOKEN_TTL,
                                                UNEXPECTED_ISSUER);

        mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                            .headers(headers)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createRequestBody()))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")));
    }

    @Test
    void jwtDecoderShouldAcceptJwtFromAllowedIssuerConfiguredInSpringContext() throws Exception {
        assertDoesNotThrow(() -> jwtDecoder.decode(validAuthToken(CONFIRMED_SECONDARY_ISSUER)));
    }

    @Test
    void jwtDecoderShouldRejectJwtFromUnexpectedIssuerConfiguredInSpringContext() throws Exception {
        String token = validAuthToken(UNEXPECTED_ISSUER);

        assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));
    }

    private String validAuthToken(String issuer) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(issuer)
            .subject("CPO_Stub")
            .issueTime(Date.from(TOKEN_ISSUED_AT))
            .expirationTime(Date.from(TOKEN_EXPIRES_AT))
            .build();
        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(KeyGenUtil.getRsaJWK().getKeyID())
                .build(),
            claims
        );
        signedJwt.sign(new RSASSASigner(KeyGenUtil.getRsaJWK()));
        return signedJwt.serialize();
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
