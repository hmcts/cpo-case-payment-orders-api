package uk.gov.hmcts.reform.cpo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
            .paths(PathSelectors.any())
            .build().useDefaultResponseMessages(false)
            .apiInfo(apiInfo())
            .globalRequestParameters(Arrays.asList(
                headerServiceAuthorization(),
                headerAuthorization()
            ));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("Case Payment Orders API")
            .description("Case payment orders")
            .version("1.0.0")
            .contact(new Contact("CDM",
                                 "https://tools.hmcts.net/confluence/display/RCCD/Reform%3A+Core+Case+Data+Home",
                                 "corecasedatateam@hmcts.net"))
            .termsOfServiceUrl("")
            .build();
    }

    private RequestParameter headerServiceAuthorization() {
        return new RequestParameterBuilder()
            .name("ServiceAuthorization")
            .description("Valid Service-to-Service JWT token for a whitelisted micro-service")
            .in(ParameterType.HEADER)
            .required(true)
            .build();
    }

    private RequestParameter headerAuthorization() {
        return new RequestParameterBuilder()
            .name("Authorization")
            .description("Keyword `Bearer` followed by a valid IDAM user token")
            .in(ParameterType.HEADER)
            .required(true)
            .build();
    }

}
