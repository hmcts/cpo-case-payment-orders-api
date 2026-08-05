package uk.gov.hmcts.reform.cpo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
public class ServiceAuthConfiguration {

    @Bean
    public AuthTokenGenerator authTokenGenerator(
        ServiceAuthorisationApi serviceAuthorisationApi,
        @Value("${idam.s2s-auth.microservice}") String microservice,
        @Value("${idam.s2s-auth.totp-secret}") String secret
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(
            secret,
            microservice,
            serviceAuthorisationApi
        );
    }
}
