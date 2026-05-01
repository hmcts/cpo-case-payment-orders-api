package uk.gov.hmcts.reform.cpo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;

import java.io.IOException;
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
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                            .header(ServiceAuthFilter.AUTHORISATION, "s2s-token"))
            .andExpect(status().isOk())
            .andExpect(content().string("authenticated"));

        verify(jwtDecoder).decode(TOKEN);
        verify(jwtGrantedAuthoritiesConverter).convert(jwt);
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
            return new ServiceAuthFilter(mock(AuthTokenValidator.class), List.of("test_service")) {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response,
                                                FilterChain filterChain)
                    throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }
    }
}
