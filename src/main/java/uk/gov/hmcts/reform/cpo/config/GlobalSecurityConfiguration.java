package uk.gov.hmcts.reform.cpo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@Profile("!itest")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class GlobalSecurityConfiguration  {

}


