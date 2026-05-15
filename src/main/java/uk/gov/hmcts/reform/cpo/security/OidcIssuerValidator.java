package uk.gov.hmcts.reform.cpo.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;

public final class OidcIssuerValidator {

    private OidcIssuerValidator() {
    }

    public static OAuth2TokenValidator<Jwt> exactIssuerValidator(String primaryIssuer, String allowedIssuers) {
        Set<String> acceptedIssuers = acceptedIssuers(primaryIssuer, allowedIssuers);
        return new JwtClaimValidator<>(JwtClaimNames.ISS,
                                       claimValue -> claimValue != null
                                           && acceptedIssuers.contains(claimValue.toString()));
    }

    public static Set<String> acceptedIssuers(String primaryIssuer, String allowedIssuers) {
        LinkedHashSet<String> acceptedIssuers = new LinkedHashSet<>();
        addPrimaryIssuer(acceptedIssuers, primaryIssuer);
        addAllowedIssuers(acceptedIssuers, allowedIssuers);
        return Collections.unmodifiableSet(acceptedIssuers);
    }

    private static void addPrimaryIssuer(Set<String> acceptedIssuers, String primaryIssuer) {
        if (primaryIssuer == null || primaryIssuer.isBlank()) {
            throw new IllegalStateException("oidc.issuer must be configured");
        }
        acceptedIssuers.add(primaryIssuer.trim());
    }

    private static void addAllowedIssuers(Set<String> acceptedIssuers, String allowedIssuers) {
        if (allowedIssuers == null || allowedIssuers.isBlank()) {
            return;
        }

        for (String allowedIssuer : allowedIssuers.split(",")) {
            String trimmedIssuer = allowedIssuer.trim();
            if (!trimmedIssuer.isEmpty()) {
                acceptedIssuers.add(trimmedIssuer);
            }
        }
    }
}
