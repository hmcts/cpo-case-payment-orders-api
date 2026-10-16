package uk.gov.hmcts.reform.cpo.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.cpo.security.SafeServiceAuthFilter;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter.TOKEN_NAME;

@WebMvcTest(controllers = SecurityConfigurationTest.TestController.class,
    excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = AuditConfiguration.class))
@Import({
    SecurityConfiguration.class,
    SecurityConfigurationTest.TestConfig.class,
    SecurityConfigurationTest.TestController.class
})
@TestPropertySource(properties = {
    "spring.security.oauth2.client.provider.oidc.issuer-uri=http://localhost/o",
    "idam.oidc.url=localhost:5000",
    "oidc.issuer=http://localhost/o"
})
class SecurityConfigurationTest {

    private static final String TOKEN = "test-jwt";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Test
    void invalidJwtDoesNotExposeDecoderDiagnostics() throws Exception {
        when(jwtDecoder.decode(TOKEN)).thenThrow(
            new org.springframework.security.oauth2.jwt.BadJwtException("decoder at http://internal-service/jwks"));
        mockMvc.perform(get("/test-secured")
                .header(ServiceAuthFilter.AUTHORISATION, "s2s-token")
                .header("Authorization", "Bearer " + TOKEN)
                .header("ServiceAuthorization", "service-token"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string("WWW-Authenticate", "Bearer"));
    }

    @Test
    void missingJwtStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/test-secured").header("ServiceAuthorization", "service-token"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    @Test
    void shouldAcceptAuthenticatedBearerJwtWithoutOAuth2ClientRegistration() throws Exception {
        Jwt jwt = Jwt.withTokenValue(TOKEN)
            .header("alg", "none")
            .claim(TOKEN_NAME, "access_token")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();

        when(jwtDecoder.decode(TOKEN)).thenReturn(jwt);
        when(jwtGrantedAuthoritiesConverter.convert(any(Jwt.class)))
            .thenReturn(List.of(new SimpleGrantedAuthority("caseworker")));

        assertThat(environment.getProperty("spring.security.oauth2.client.registration.oidc.client-id")).isNull();
        assertThat(environment.getProperty("spring.security.oauth2.client.registration.oidc.client-secret")).isNull();

        mockMvc.perform(get("/test-secured")
                .header(ServiceAuthFilter.AUTHORISATION, "s2s-token")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                            .header(ServiceAuthFilter.AUTHORISATION, "s2s-token"))
            .andExpect(status().isOk())
            .andExpect(content().string("authenticated"));

        verify(jwtDecoder).decode(TOKEN);
        verify(jwtGrantedAuthoritiesConverter).convert(jwt);
    }

    @Test
    void serviceFailuresAreSafeThroughTheCompleteSecurityChain() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(SafeServiceAuthFilter.class);
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Level previousLevel = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);
        try {
            logger.setLevel(Level.INFO);
            mockMvc.perform(get("/test-secured")
                    .header("Authorization", "Bearer " + TOKEN)
                    .header(ServiceAuthFilter.AUTHORISATION, "invalid-service-token"))
                .andExpect(status().isUnauthorized()).andExpect(content().string(""));
            mockMvc.perform(get("/test-secured")
                    .header("Authorization", "Bearer " + TOKEN)
                    .header(ServiceAuthFilter.AUTHORISATION, "denied-service-token"))
                .andExpect(status().isForbidden()).andExpect(content().string(""));
            assertThat(appender.list).extracting(ILoggingEvent::getFormattedMessage).contains(
                "Service authentication failed: code=AUTHENTICATION_REQUIRED status=401",
                "Service access denied: code=ACCESS_DENIED status=403");
            assertThat(appender.list).filteredOn(event -> event.getLevel().isGreaterOrEqual(Level.WARN))
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain("Synthetic", "private-service-id");
                    assertThat(event.getThrowableProxy()).isNull();
                });
        } finally {
            logger.setLevel(previousLevel);
            rootLogger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void idamOutagesStay500AndDoNotEscapeTheSecurityChain() throws Exception {
        Jwt jwt = Jwt.withTokenValue(TOKEN).header("alg", "none").claim(TOKEN_NAME, "access_token").build();
        when(jwtDecoder.decode(TOKEN)).thenReturn(jwt);
        when(jwtGrantedAuthoritiesConverter.convert(any(Jwt.class))).thenThrow(
            new org.springframework.security.authentication.AuthenticationServiceException("IDAM error",
                new IllegalStateException("Synthetic private IDAM detail")));
        Logger logger = (Logger) LoggerFactory.getLogger(
            uk.gov.hmcts.reform.cpo.security.SafeAuthenticationErrors.class);
        Logger converterLogger = (Logger) LoggerFactory.getLogger(JwtGrantedAuthoritiesConverter.class);
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Level previousLevel = logger.getLevel();
        Level previousConverterLevel = converterLogger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);
        try {
            converterLogger.setLevel(Level.INFO);
            for (Level level : List.of(Level.INFO, Level.DEBUG)) {
                logger.setLevel(level);
                appender.list.clear();
                mockMvc.perform(get("/test-secured")
                        .header("Authorization", "Bearer " + TOKEN)
                        .header(ServiceAuthFilter.AUTHORISATION, "service-token"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(""))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .doesNotExist("WWW-Authenticate"));
                assertThat(appender.list).extracting(ILoggingEvent::getFormattedMessage)
                    .contains("Authentication service failed: code=INTERNAL_ERROR status=500");
                assertThat(appender.list).filteredOn(event -> event.getLevel().isGreaterOrEqual(Level.WARN))
                    .allSatisfy(event -> {
                        assertThat(event.getFormattedMessage()).doesNotContain("Synthetic private IDAM detail");
                        assertThat(event.getThrowableProxy()).isNull();
                    });
                if (level == Level.DEBUG) {
                    assertThat(appender.list).filteredOn(event -> event.getLoggerName().equals(logger.getName())
                        && event.getLevel() == Level.DEBUG).anySatisfy(event ->
                            assertThat(event.getThrowableProxy().getCause().getMessage())
                                .isEqualTo("Synthetic private IDAM detail"));
                }
            }
        } finally {
            logger.setLevel(previousLevel);
            converterLogger.setLevel(previousConverterLevel);
            rootLogger.detachAppender(appender);
            appender.stop();
        }
    }

    @RestController
    public static class TestController {

        @GetMapping("/test-secured")
        String secured() {
            return "authenticated";
        }
    }

    static class TestConfig {

        @Bean
        ServiceAuthFilter serviceAuthFilter() {
            AuthTokenValidator validator = mock(AuthTokenValidator.class);
            when(validator.getServiceName(org.mockito.ArgumentMatchers.anyString())).thenAnswer(invocation -> {
                String token = invocation.getArgument(0);
                if (token.equals("Bearer invalid-service-token")) {
                    throw new InvalidTokenException("Synthetic internal S2S diagnostic");
                }
                return token.equals("Bearer denied-service-token") ? "private-service-id" : "test_service";
            });
            return new SafeServiceAuthFilter(validator, List.of("test_service"));
        }
    }
}
