package uk.gov.hmcts.reform.cpo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cpo.security.OidcIssuerValidator;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
public class SecurityConfiguration {

    private final String issuerUri;
    private final String expectedIssuer;
    private final String allowedIssuers;
    private final ServiceAuthFilter serviceAuthFilter;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    private static final String[] AUTH_ALLOWED_LIST = {
        "/**/webjars/**",
        "/**/v3/api-docs/**",
        "/swagger-resources/**",
        "/swagger-ui/**",
        "/webjars/**",
        "/v3/api-docs",
        "/health",
        "/health/liveness",
        "/health/readiness",
        "/info",
        "/favicon.ico",
        "/"
    };

    public SecurityConfiguration(
        @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}") final String issuerUri,
        @Value("${oidc.issuer}") final String expectedIssuer,
        @Value("${oidc.allowed-issuers:}") final String allowedIssuers,
        final ServiceAuthFilter serviceAuthFilter,
        final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter
    ) {
        this.issuerUri = issuerUri;
        this.expectedIssuer = expectedIssuer;
        this.allowedIssuers = allowedIssuers;
        this.serviceAuthFilter = serviceAuthFilter;
        jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);
        // Discover metadata from issuer-uri, but enforce bearer token issuers via oidc.issuer first.
        jwtDecoder.setJwtValidator(jwtValidator(expectedIssuer, allowedIssuers));
        return jwtDecoder;
    }

    static OAuth2TokenValidator<Jwt> jwtValidator(String expectedIssuer, String allowedIssuers) {
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> withIssuer = OidcIssuerValidator.exactIssuerValidator(expectedIssuer,
                                                                                        allowedIssuers);
        return new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuer);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
            .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(STATELESS))
            .csrf((csrf) -> csrf.disable())
            .formLogin((formLogin) -> formLogin.disable())
            .logout((logout) -> logout.disable())
            .authorizeHttpRequests((authHttp) -> authHttp.anyRequest().authenticated())
            .oauth2ResourceServer((oauth2) ->
                                      oauth2.jwt((jwt) ->
                                                     jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(AUTH_ALLOWED_LIST);
    }

}
