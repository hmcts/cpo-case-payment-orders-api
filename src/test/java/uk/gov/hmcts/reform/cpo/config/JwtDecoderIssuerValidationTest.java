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
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtDecoderIssuerValidationTest {

    private static final String VALID_ISSUER = "http://fr-am:8080/openam/oauth2/hmcts";
    private static final String INVALID_ISSUER = "http://unexpected-issuer";
    private static final KeyPair RSA_KEY_PAIR = generateKeyPair();

    @Test
    void shouldRejectJwtFromUnexpectedIssuer() throws JOSEException {
        JwtValidationException exception = assertThrows(
            JwtValidationException.class,
            () -> decoder().decode(buildSignedToken(INVALID_ISSUER, Instant.now().plusSeconds(300)))
        );

        assertThat(exception.getMessage()).contains("iss");
    }

    private OAuth2TokenValidator<Jwt> validator() {
        return new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            new JwtIssuerValidator(VALID_ISSUER)
        );
    }

    private NimbusJwtDecoder decoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) RSA_KEY_PAIR.getPublic()).build();
        jwtDecoder.setJwtValidator(validator());
        return jwtDecoder;
    }

    private String buildSignedToken(String issuer, Instant expiresAt) throws JOSEException {
        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject("user")
                .issueTime(Date.from(expiresAt.minusSeconds(60)))
                .expirationTime(Date.from(expiresAt))
                .build()
        );
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
