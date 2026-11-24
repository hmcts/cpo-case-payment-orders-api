package uk.gov.hmcts.reform.cpo.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class OidcIssuerConfiguration {

    private OidcIssuerConfiguration() {
    }

    public static Set<String> allowedIssuers(String primaryIssuer, String configuredAllowedIssuers) {
        if (primaryIssuer == null || primaryIssuer.isBlank()) {
            throw new IllegalStateException("oidc.issuer must be configured");
        }

        LinkedHashSet<String> issuers = new LinkedHashSet<>();
        issuers.add(primaryIssuer.trim());
        addAllowedIssuers(issuers, configuredAllowedIssuers);
        return Collections.unmodifiableSet(issuers);
    }

    private static void addAllowedIssuers(Set<String> issuers, String configuredAllowedIssuers) {
        if (configuredAllowedIssuers == null || configuredAllowedIssuers.isBlank()) {
            return;
        }

        for (String configuredAllowedIssuer : configuredAllowedIssuers.split(",")) {
            String issuer = configuredAllowedIssuer.trim();
            if (!issuer.isEmpty()) {
                issuers.add(issuer);
            }
        }
    }
}
