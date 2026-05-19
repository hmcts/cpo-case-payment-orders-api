package uk.gov.hmcts.reform.cpo.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OidcIssuerConfigurationTest {

    @Test
    void shouldUsePrimaryIssuerWhenAllowedIssuersAreUnset() {
        assertThat(OidcIssuerConfiguration.allowedIssuers(" primary ", null))
            .containsExactly("primary");
    }

    @Test
    void shouldUsePrimaryIssuerWhenAllowedIssuersAreBlank() {
        assertThat(OidcIssuerConfiguration.allowedIssuers("primary", " "))
            .containsExactly("primary");
    }

    @Test
    void shouldIncludePrimaryAndConfiguredAllowedIssuersWithoutDuplicatesOrBlankEntries() {
        assertThat(OidcIssuerConfiguration.allowedIssuers("primary",
                                                          " secondary, , tertiary , secondary, primary "))
            .containsExactly("primary", "secondary", "tertiary");
    }

    @Test
    void shouldRejectBlankPrimaryIssuerEvenWhenAllowedIssuersAreConfigured() {
        assertThatThrownBy(() -> OidcIssuerConfiguration.allowedIssuers(" ", "secondary"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("oidc.issuer must be configured");
    }
}
