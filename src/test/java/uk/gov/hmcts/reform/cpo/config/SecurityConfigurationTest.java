package uk.gov.hmcts.reform.cpo.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class SecurityConfigurationTest {

    private static final String ISSUER_URI = "http://idam-discovery/o";
    private static final String VALID_ISSUER = "http://fr-am:8080/openam/oauth2/hmcts";
    private static final String SECONDARY_ISSUER = "https://idam-web-public.aat.platform.hmcts.net/o";
    private static final String TERTIARY_ISSUER = "http://idam-api:5000/o";
    private static final String INVALID_ISSUER = "http://unexpected-issuer";
    private static final Instant TOKEN_ISSUED_AT = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant VALID_TOKEN_EXPIRES_AT = Instant.parse("2099-01-01T00:00:00Z");
    private static final Instant EXPIRED_TOKEN_EXPIRES_AT = Instant.parse("2024-01-01T01:00:00Z");
    private static final KeyPair RSA_KEY_PAIR = generateKeyPair();

    @Test
    void jwtDecoderShouldAcceptJwtFromConfiguredAllowedIssuer() throws JOSEException {
        JwtDecoder jwtDecoder = jwtDecoder(SECONDARY_ISSUER);

        assertDoesNotThrow(() -> jwtDecoder.decode(buildSignedToken(SECONDARY_ISSUER, VALID_TOKEN_EXPIRES_AT)));
    }

    @Test
    void jwtDecoderShouldRejectJwtFromUnexpectedIssuer() throws JOSEException {
        JwtDecoder jwtDecoder = jwtDecoder(SECONDARY_ISSUER);

        assertThrows(JwtValidationException.class,
                     () -> jwtDecoder.decode(buildSignedToken(INVALID_ISSUER, VALID_TOKEN_EXPIRES_AT)));
    }

    @Test
    void shouldAcceptJwtFromConfiguredIssuerWhenAllowedIssuersAreNotSet() {
        assertFalse(validator(null).validate(buildJwt(VALID_ISSUER, VALID_TOKEN_EXPIRES_AT)).hasErrors());
    }

    @Test
    void shouldKeepPrimaryIssuerAcceptedWhenAllowedIssuersAreConfigured() {
        assertFalse(validator(SECONDARY_ISSUER)
                        .validate(buildJwt(VALID_ISSUER, VALID_TOKEN_EXPIRES_AT))
                        .hasErrors());
    }

    @Test
    void shouldAcceptJwtFromTrimmedAllowedIssuer() {
        assertFalse(validator(" " + SECONDARY_ISSUER + " ")
                        .validate(buildJwt(SECONDARY_ISSUER, VALID_TOKEN_EXPIRES_AT))
                        .hasErrors());
    }

    @Test
    void shouldAcceptJwtFromCommaSeparatedAllowedIssuerList() {
        assertFalse(validator(SECONDARY_ISSUER + ", " + TERTIARY_ISSUER)
                        .validate(buildJwt(TERTIARY_ISSUER, VALID_TOKEN_EXPIRES_AT))
                        .hasErrors());
    }

    @Test
    void shouldRejectIssuerThatOnlyPartiallyMatchesAllowedIssuer() {
        assertTrue(validator(SECONDARY_ISSUER)
                       .validate(buildJwt(SECONDARY_ISSUER + "/extra", VALID_TOKEN_EXPIRES_AT))
                       .hasErrors());
    }

    @Test
    void shouldRejectJwtFromUnexpectedIssuer() {
        assertTrue(validator("").validate(buildJwt(INVALID_ISSUER, VALID_TOKEN_EXPIRES_AT)).hasErrors());
    }

    @Test
    void shouldRejectJwtWithoutIssuer() {
        assertTrue(validator("").validate(buildJwtWithoutIssuer(VALID_TOKEN_EXPIRES_AT)).hasErrors());
    }

    @Test
    void shouldRejectExpiredJwtEvenWhenIssuerMatches() {
        assertTrue(validator("").validate(buildJwt(VALID_ISSUER, EXPIRED_TOKEN_EXPIRES_AT)).hasErrors());
    }

    @Test
    void shouldFailFastWhenPrimaryIssuerIsBlank() {
        assertThrows(IllegalStateException.class, () -> SecurityConfiguration.jwtValidator(" ", ""));
    }

    private OAuth2TokenValidator<Jwt> validator(String allowedIssuers) {
        return SecurityConfiguration.jwtValidator(VALID_ISSUER, allowedIssuers);
    }

    private Jwt buildJwt(String issuer, Instant expiresAt) {
        return Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .issuer(issuer)
            .subject("user")
            .issuedAt(TOKEN_ISSUED_AT)
            .expiresAt(expiresAt)
            .build();
    }

    private Jwt buildJwtWithoutIssuer(Instant expiresAt) {
        return Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("user")
            .issuedAt(TOKEN_ISSUED_AT)
            .expiresAt(expiresAt)
            .build();
    }

    private JwtDecoder jwtDecoder(String allowedIssuers) {
        SecurityConfiguration securityConfiguration = securityConfiguration(allowedIssuers);
        NimbusJwtDecoder discoveredDecoder = NimbusJwtDecoder
            .withPublicKey((RSAPublicKey) RSA_KEY_PAIR.getPublic())
            .build();

        try (MockedStatic<JwtDecoders> jwtDecoders = mockStatic(JwtDecoders.class)) {
            jwtDecoders.when(() -> JwtDecoders.fromOidcIssuerLocation(ISSUER_URI)).thenReturn(discoveredDecoder);

            JwtDecoder jwtDecoder = securityConfiguration.jwtDecoder();

            jwtDecoders.verify(() -> JwtDecoders.fromOidcIssuerLocation(ISSUER_URI));
            return jwtDecoder;
        }
    }

    private SecurityConfiguration securityConfiguration(String allowedIssuers) {
        return new SecurityConfiguration(
            ISSUER_URI,
            VALID_ISSUER,
            allowedIssuers,
            mock(ServiceAuthFilter.class),
            mock(JwtGrantedAuthoritiesConverter.class)
        );
    }

    private String buildSignedToken(String issuer, Instant expiresAt) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(issuer)
            .subject("user")
            .issueTime(Date.from(TOKEN_ISSUED_AT))
            .expirationTime(Date.from(expiresAt))
            .build();
        SignedJWT signedJwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), claims);
        signedJwt.sign(new RSASSASigner((RSAPrivateKey) RSA_KEY_PAIR.getPrivate()));
        return signedJwt.serialize();
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key pair for JWT test", e);
        }
    }
}
