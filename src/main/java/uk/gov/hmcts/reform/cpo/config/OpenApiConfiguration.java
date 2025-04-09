package uk.gov.hmcts.reform.cpo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public GroupedOpenApi grouped() {
        //return GroupedOpenApi.builder()
        //    .group("Case Payment Orders API")
        //    .addOpenApiMethodFilter(method -> method.isAnnotationPresent(RestController.class))
        //    .build();

        return GroupedOpenApi.builder()
            .group("OpenApiController")
            .packagesToScan("uk.gov.hmcts.reform.cpo.controllers")
            .build();
    }

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(apiInfo())
            .externalDocs(externalDocs())
            .components(components())
            ;
    }

    private Info apiInfo() {
        return new Info()
            .title("Case Payment Orders API")
            .description("Case payment orders")
            .version("v1.0.0")
            .license(new License()
                         .name("MIT")
                         .url("https://opensource.org/licenses/MIT")
            )
            .contact(new Contact()
                         .name("CDM")
                         .url("https://tools.hmcts.net/confluence/display/RCCD/Reform%3A+Core+Case+Data+Home")
                         .email("corecasedatateam@hmcts.net")
            );
    }

    private ExternalDocumentation externalDocs() {
        return new ExternalDocumentation()
            .description("README")
            .url("https://github.com/hmcts/rpe-pdf-service");
    }

    private Components components() {
        return new Components()
            .addSecuritySchemes("headerServiceAuthorization", headerServiceAuthorization())
            .addSecuritySchemes("headerAuthorization", headerAuthorization())
            ;
    }

    private SecurityScheme headerServiceAuthorization() {
        return new SecurityScheme()
            .name("ServiceAuthorization")
            .description("Valid Service-to-Service JWT token for a whitelisted micro-service")
            .in(SecurityScheme.In.HEADER);
    }

    private SecurityScheme headerAuthorization() {
        return new SecurityScheme()
            .name("Authorization")
            .description("Keyword `Bearer` followed by a valid IDAM user token")
            .in(SecurityScheme.In.HEADER);
    }

    //CCD-3509 CVE-2021-22044 required to fix null pointers in integration tests,
    //conflict in Springfox after Springboot 2.6.10
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier,
         ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier,
         EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties,
         WebEndpointProperties webEndpointProperties, Environment environment) {

        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
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
