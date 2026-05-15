package uk.gov.hmcts.reform.cpo.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import uk.gov.hmcts.reform.cpo.security.OidcIssuerValidator;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtDecoderIssuerValidationTest {

    private static final String VALID_ISSUER = "http://fr-am:8080/openam/oauth2/hmcts";
    private static final String SECONDARY_ISSUER = "https://idam-web-public.aat.platform.hmcts.net/o";
    private static final String INVALID_ISSUER = "http://unexpected-issuer";
    private static final KeyPair RSA_KEY_PAIR = generateKeyPair();
    private static final KeyPair OTHER_RSA_KEY_PAIR = generateKeyPair();

    @Test
    void shouldAcceptJwtFromConfiguredIssuerWhenAllowedIssuersAreNotSet() throws JOSEException {
        assertDoesNotThrow(() -> decoder().decode(buildSignedToken(VALID_ISSUER, Instant.now().plusSeconds(300))));
    }

    @Test
    void shouldAcceptJwtFromPrimaryIssuerWhenAllowedIssuersAreConfigured() throws JOSEException {
        assertDoesNotThrow(() -> decoder(SECONDARY_ISSUER)
            .decode(buildSignedToken(VALID_ISSUER, Instant.now().plusSeconds(300))));
    }

    @Test
    void shouldAcceptJwtFromAllowedIssuer() throws JOSEException {
        assertDoesNotThrow(() -> decoder(" " + SECONDARY_ISSUER + " ")
            .decode(buildSignedToken(SECONDARY_ISSUER, Instant.now().plusSeconds(300))));
    }

    @Test
    void shouldRejectJwtFromIssuerThatOnlyPartiallyMatchesAllowedIssuer() throws JOSEException {
        JwtValidationException exception = assertThrows(
            JwtValidationException.class,
            () -> decoder(SECONDARY_ISSUER)
                .decode(buildSignedToken(SECONDARY_ISSUER + "/extra", Instant.now().plusSeconds(300)))
        );

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectJwtFromUnexpectedIssuer() throws JOSEException {
        JwtValidationException exception = assertThrows(
            JwtValidationException.class,
            () -> decoder().decode(buildSignedToken(INVALID_ISSUER, Instant.now().plusSeconds(300)))
        );

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectJwtWithoutIssuer() throws JOSEException {
        JwtValidationException exception = assertThrows(
            JwtValidationException.class,
            () -> decoder().decode(buildSignedToken(null, Instant.now().plusSeconds(300)))
        );

        assertThat(exception.getMessage()).contains("iss");
    }

    @Test
    void shouldRejectExpiredJwtEvenWhenIssuerMatches() throws JOSEException {
        JwtValidationException exception = assertThrows(
            JwtValidationException.class,
            () -> decoder().decode(buildSignedToken(VALID_ISSUER, Instant.now().minusSeconds(60)))
        );

        assertThat(exception.getMessage()).contains("Jwt expired");
    }

    @Test
    void shouldRejectJwtWithInvalidSignature() throws JOSEException {
        assertThrows(
            JwtException.class,
            () -> decoder().decode(buildSignedToken(VALID_ISSUER,
                                                    Instant.now().plusSeconds(300),
                                                    (RSAPrivateKey) OTHER_RSA_KEY_PAIR.getPrivate()))
        );
    }

    private OAuth2TokenValidator<Jwt> validator() {
        return validator("");
    }

    private OAuth2TokenValidator<Jwt> validator(String allowedIssuers) {
        return new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            OidcIssuerValidator.exactIssuerValidator(VALID_ISSUER, allowedIssuers)
        );
    }

    private NimbusJwtDecoder decoder() {
        return decoder("");
    }

    private NimbusJwtDecoder decoder(String allowedIssuers) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) RSA_KEY_PAIR.getPublic()).build();
        jwtDecoder.setJwtValidator(validator(allowedIssuers));
        return jwtDecoder;
    }

    private String buildSignedToken(String issuer, Instant expiresAt) throws JOSEException {
        return buildSignedToken(issuer, expiresAt, (RSAPrivateKey) RSA_KEY_PAIR.getPrivate());
    }

    private String buildSignedToken(String issuer, Instant expiresAt, RSAPrivateKey signingKey) throws JOSEException {
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
            .subject("user")
            .issueTime(Date.from(expiresAt.minusSeconds(60)))
            .expirationTime(Date.from(expiresAt));

        if (issuer != null) {
            claimsBuilder.issuer(issuer);
        }

        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
            claimsBuilder.build()
        );
        signedJwt.sign(new RSASSASigner(signingKey));
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
