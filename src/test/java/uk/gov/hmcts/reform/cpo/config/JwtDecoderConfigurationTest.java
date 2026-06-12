package uk.gov.hmcts.reform.cpo.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class JwtDecoderConfigurationTest {

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
    void shouldAcceptJwtFromConfiguredIssuerWhenAllowedIssuersAreNotSet() {
        JwtDecoder jwtDecoder = jwtDecoder(VALID_ISSUER, null);
        String token = buildSignedToken(VALID_ISSUER, VALID_TOKEN_EXPIRES_AT);

        assertDoesNotThrow(() -> jwtDecoder.decode(token));
    }

    @Test
    void shouldKeepPrimaryIssuerAcceptedWhenAllowedIssuersAreConfigured() {
        JwtDecoder jwtDecoder = jwtDecoder(VALID_ISSUER, SECONDARY_ISSUER);
        String token = buildSignedToken(VALID_ISSUER, VALID_TOKEN_EXPIRES_AT);

        assertDoesNotThrow(() -> jwtDecoder.decode(token));
    }

    @Test
    void shouldAcceptJwtFromTrimmedAllowedIssuer() {
        JwtDecoder jwtDecoder = jwtDecoder(VALID_ISSUER, " " + SECONDARY_ISSUER + " ");
        String token = buildSignedToken(SECONDARY_ISSUER, VALID_TOKEN_EXPIRES_AT);

        assertDoesNotThrow(() -> jwtDecoder.decode(token));
    }

    @Test
    void shouldAcceptJwtFromCommaSeparatedAllowedIssuerList() {
        JwtDecoder jwtDecoder = jwtDecoder(VALID_ISSUER, SECONDARY_ISSUER + ", " + TERTIARY_ISSUER);
        String token = buildSignedToken(TERTIARY_ISSUER, VALID_TOKEN_EXPIRES_AT);

        assertDoesNotThrow(() -> jwtDecoder.decode(token));
    }

    @Test
    void shouldRejectIssuerThatOnlyPartiallyMatchesAllowedIssuer() {
        JwtDecoder jwtDecoder = jwtDecoder(VALID_ISSUER, SECONDARY_ISSUER);
        String token = buildSignedToken(SECONDARY_ISSUER + "/extra", VALID_TOKEN_EXPIRES_AT);

        assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void shouldRejectJwtFromUnexpectedIssuer() {
        JwtDecoder jwtDecoder = jwtDecoder(VALID_ISSUER, "");
        String token = buildSignedToken(INVALID_ISSUER, VALID_TOKEN_EXPIRES_AT);

        assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void shouldRejectJwtWithoutIssuer() {
        JwtDecoder jwtDecoder = jwtDecoder(VALID_ISSUER, "");
        String token = buildSignedTokenWithoutIssuer(VALID_TOKEN_EXPIRES_AT);

        assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void shouldRejectExpiredJwtEvenWhenIssuerMatches() {
        JwtDecoder jwtDecoder = jwtDecoder(VALID_ISSUER, "");
        String token = buildSignedToken(VALID_ISSUER, EXPIRED_TOKEN_EXPIRES_AT);

        assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void shouldFailFastWhenPrimaryIssuerIsBlank() {
        assertThrows(IllegalStateException.class, () -> jwtDecoder(" ", ""));
    }

    private JwtDecoder jwtDecoder(String primaryIssuer, String allowedIssuers) {
        SecurityConfiguration securityConfiguration = securityConfiguration(primaryIssuer, allowedIssuers);
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

    private SecurityConfiguration securityConfiguration(String primaryIssuer, String allowedIssuers) {
        return new SecurityConfiguration(
            ISSUER_URI,
            primaryIssuer,
            allowedIssuers,
            mock(ServiceAuthFilter.class),
            mock(JwtGrantedAuthoritiesConverter.class)
        );
    }

    private String buildSignedToken(String issuer, Instant expiresAt) {
        return buildSignedToken(issuer, expiresAt, true);
    }

    private String buildSignedTokenWithoutIssuer(Instant expiresAt) {
        return buildSignedToken(null, expiresAt, false);
    }

    private String buildSignedToken(String issuer, Instant expiresAt, boolean includeIssuer) {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
            .subject("user")
            .issueTime(Date.from(TOKEN_ISSUED_AT))
            .expirationTime(Date.from(expiresAt));
        if (includeIssuer) {
            claims.issuer(issuer);
        }

        SignedJWT signedJwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), claims.build());
        try {
            signedJwt.sign(new RSASSASigner((RSAPrivateKey) RSA_KEY_PAIR.getPrivate()));
            return signedJwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign JWT for test", e);
        }
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
