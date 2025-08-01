package uk.gov.hmcts.reform.cpo.wiremock;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.cpo.wiremock.extension.ConnectionClosedTransformer;
import uk.gov.hmcts.reform.cpo.wiremock.extension.DynamicOAuthJwkSetResponseTransformer;
import uk.gov.hmcts.reform.cpo.wiremock.extension.DynamicS2sDetailsResponseTransformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class WireMockTestConfiguration {

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> {
            options.extensions(
                new ConnectionClosedTransformer(),
                new DynamicS2sDetailsResponseTransformer(),
                new DynamicOAuthJwkSetResponseTransformer()
            );
        };
    }

    //CCD-3509 CVE-2021-22044 required to fix null pointers in integration tests,
    //conflict in Springfox after Springboot 2.6.10
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier,
         EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties,
         WebEndpointProperties webEndpointProperties, Environment environment) {

        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>(webEndpoints);
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment,
                                                                             basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
                                                corsProperties.toCorsConfiguration(),
                                                new EndpointLinksResolver(allEndpoints, basePath),
                                                shouldRegisterLinksMapping);
    }

    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment,
                                               String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath)
            || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }

}
