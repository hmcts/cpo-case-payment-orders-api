package uk.gov.hmcts.reform.cpo.wiremock;

import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.cpo.wiremock.extension.DynamicOAuthJwkSetResponseTransformer;
import uk.gov.hmcts.reform.cpo.wiremock.extension.DynamicS2sDetailsResponseTransformer;

@Configuration
public class WireMockTestConfiguration {

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> {
            options.extensions(
                new DynamicS2sDetailsResponseTransformer(),
                new DynamicOAuthJwkSetResponseTransformer()
            );
        };
    }

}
