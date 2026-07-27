package uk.gov.hmcts.reform.cpo.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

public class KeyGenUtil {

    private static final String KEY_ID = "23456789";
    private static final RSAKey RSA_JWK = generateKey();

    private KeyGenUtil() {
    }

    public static RSAKey getRsaJWK() {
        return RSA_JWK;
    }

    private static RSAKey generateKey() {
        try {
            return new RSAKeyGenerator(2048)
                .keyID(KEY_ID)
                .generate();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Unable to generate integration-test RSA key", exception);
        }
    }

}
