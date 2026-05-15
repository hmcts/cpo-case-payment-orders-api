package uk.gov.hmcts.reform.cpo.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

import uk.gov.hmcts.reform.cpo.security.OidcIssuerValidator;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigurationTest {

    private static final String VALID_ISSUER = "http://fr-am:8080/openam/oauth2/hmcts";
    private static final String SECONDARY_ISSUER = "https://idam-web-public.aat.platform.hmcts.net/o";
    private static final String TERTIARY_ISSUER = "http://idam-api:5000/o";
    private static final String INVALID_ISSUER = "http://unexpected-issuer";

    @Test
    void shouldAcceptJwtFromConfiguredIssuerWhenAllowedIssuersAreNotSet() {
        assertFalse(validator(null).validate(buildJwt(VALID_ISSUER, Instant.now().plusSeconds(300))).hasErrors());
    }

    @Test
    void shouldKeepPrimaryIssuerAcceptedWhenAllowedIssuersAreConfigured() {
        assertFalse(validator(SECONDARY_ISSUER)
                        .validate(buildJwt(VALID_ISSUER, Instant.now().plusSeconds(300)))
                        .hasErrors());
    }

    @Test
    void shouldAcceptJwtFromConfirmedAllowedIssuer() {
        assertFalse(validator(" " + SECONDARY_ISSUER + " ")
                        .validate(buildJwt(SECONDARY_ISSUER, Instant.now().plusSeconds(300)))
                        .hasErrors());
    }

    @Test
    void shouldAcceptJwtFromCommaSeparatedAllowedIssuerList() {
        assertFalse(validator(SECONDARY_ISSUER + ", " + TERTIARY_ISSUER)
                        .validate(buildJwt(TERTIARY_ISSUER, Instant.now().plusSeconds(300)))
                        .hasErrors());
    }

    @Test
    void shouldRejectIssuerThatOnlyPartiallyMatchesAllowedIssuer() {
        assertTrue(validator(SECONDARY_ISSUER)
                       .validate(buildJwt(SECONDARY_ISSUER + "/extra", Instant.now().plusSeconds(300)))
                       .hasErrors());
    }

    @Test
    void shouldRejectJwtFromUnexpectedIssuer() {
        assertTrue(validator("").validate(buildJwt(INVALID_ISSUER, Instant.now().plusSeconds(300))).hasErrors());
    }

    @Test
    void shouldRejectJwtWithoutIssuer() {
        assertTrue(validator("").validate(buildJwtWithoutIssuer(Instant.now().plusSeconds(300))).hasErrors());
    }

    @Test
    void shouldRejectExpiredJwtEvenWhenIssuerMatches() {
        assertTrue(validator("").validate(buildJwt(VALID_ISSUER, Instant.now().minusSeconds(60))).hasErrors());
    }

    private OAuth2TokenValidator<Jwt> validator(String allowedIssuers) {
        return new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            OidcIssuerValidator.exactIssuerValidator(VALID_ISSUER, allowedIssuers)
        );
    }

    private Jwt buildJwt(String issuer, Instant expiresAt) {
        return Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .issuer(issuer)
            .subject("user")
            .issuedAt(expiresAt.minusSeconds(60))
            .expiresAt(expiresAt)
            .build();
    }

    private Jwt buildJwtWithoutIssuer(Instant expiresAt) {
        return Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("user")
            .issuedAt(expiresAt.minusSeconds(60))
            .expiresAt(expiresAt)
            .build();
    }
}
