package uk.gov.hmcts.reform.cpo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfiguration {

    @Bean
    public GroupedOpenApi grouped() {
        return GroupedOpenApi.builder()
            .group("Case Payment Orders API")
            .pathsToMatch("/**")
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

}
