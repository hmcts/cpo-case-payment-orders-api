package uk.gov.hmcts.reform.cpo.wiremock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.wiremock.extension.DynamicOAuthJwkSetResponseTransformer;
import uk.gov.hmcts.reform.cpo.wiremock.extension.DynamicS2sDetailsResponseTransformer;

@Configuration
public class WireMockTestConfiguration {

    private final SecurityUtils securityUtils;

    @Autowired
    public WireMockTestConfiguration(@Lazy SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> {
            options.extensions(
                new DynamicS2sDetailsResponseTransformer(securityUtils),
                new DynamicOAuthJwkSetResponseTransformer()
            );
        };
    }

}
