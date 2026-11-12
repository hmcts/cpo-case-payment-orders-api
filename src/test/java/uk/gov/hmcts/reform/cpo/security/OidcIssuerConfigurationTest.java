package uk.gov.hmcts.reform.cpo.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OidcIssuerConfigurationTest {

    private static final String PRIMARY_ISSUER = "http://fr-am:8080/openam/oauth2/hmcts";
    private static final String SECONDARY_ISSUER = "https://idam-web-public.aat.platform.hmcts.net/o";
    private static final String TERTIARY_ISSUER = "http://idam-api:5000/o";

    @Test
    void shouldTrimPrimaryIssuer() {
        assertThat(OidcIssuerConfiguration.allowedIssuers(" " + PRIMARY_ISSUER + " ", ""))
            .containsExactly(PRIMARY_ISSUER);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldUsePrimaryIssuerWhenAllowedIssuersAreBlank(String allowedIssuers) {
        assertThat(OidcIssuerConfiguration.allowedIssuers(PRIMARY_ISSUER, allowedIssuers))
            .containsExactly(PRIMARY_ISSUER);
    }

    @Test
    void shouldTrimConfiguredAllowedIssuers() {
        assertThat(OidcIssuerConfiguration.allowedIssuers(PRIMARY_ISSUER,
                                                          " " + SECONDARY_ISSUER + ", " + TERTIARY_ISSUER + " "))
            .containsExactly(PRIMARY_ISSUER, SECONDARY_ISSUER, TERTIARY_ISSUER);
    }

    @ParameterizedTest
    @MethodSource("allowedIssuerListsWithBlankEntries")
    void shouldIgnoreBlankEntriesInConfiguredAllowedIssuerList(String allowedIssuers, String[] expectedIssuers) {
        assertThat(OidcIssuerConfiguration.allowedIssuers(PRIMARY_ISSUER, allowedIssuers))
            .containsExactly(expectedIssuers);
    }

    @Test
    void shouldRemoveDuplicateConfiguredAllowedIssuers() {
        assertThat(OidcIssuerConfiguration.allowedIssuers(
            PRIMARY_ISSUER,
            SECONDARY_ISSUER + ", " + TERTIARY_ISSUER + ", " + SECONDARY_ISSUER + ", " + PRIMARY_ISSUER
        ))
            .containsExactly(PRIMARY_ISSUER, SECONDARY_ISSUER, TERTIARY_ISSUER);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldRejectBlankPrimaryIssuerEvenWhenAllowedIssuersAreConfigured(String primaryIssuer) {
        assertThatThrownBy(() -> OidcIssuerConfiguration.allowedIssuers(primaryIssuer, SECONDARY_ISSUER))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("oidc.issuer must be configured");
    }

    private static Stream<Arguments> allowedIssuerListsWithBlankEntries() {
        String[] primaryAndSecondaryIssuers = {PRIMARY_ISSUER, SECONDARY_ISSUER};
        String[] allIssuers = {PRIMARY_ISSUER, SECONDARY_ISSUER, TERTIARY_ISSUER};

        return Stream.of(
            Arguments.of(SECONDARY_ISSUER, primaryAndSecondaryIssuers),
            Arguments.of(", " + SECONDARY_ISSUER, primaryAndSecondaryIssuers),
            Arguments.of(SECONDARY_ISSUER + ",", primaryAndSecondaryIssuers),
            Arguments.of(SECONDARY_ISSUER + ",," + TERTIARY_ISSUER, allIssuers)
        );
    }
}
