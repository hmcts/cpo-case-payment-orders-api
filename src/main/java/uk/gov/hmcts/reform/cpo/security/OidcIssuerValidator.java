package uk.gov.hmcts.reform.cpo.security;

import java.util.Set;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;

public final class OidcIssuerValidator {

    private OidcIssuerValidator() {
    }

    public static OAuth2TokenValidator<Jwt> exactIssuerValidator(String primaryIssuer, String allowedIssuers) {
        Set<String> acceptedIssuers = OidcIssuerConfiguration.allowedIssuers(primaryIssuer, allowedIssuers);
        return new JwtClaimValidator<>(JwtClaimNames.ISS,
                                       claimValue -> claimValue != null
                                           && acceptedIssuers.contains(claimValue.toString()));
    }
}
