package uk.gov.hmcts.reform.cpo.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter.BEARER;
import static uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter.TOKEN_NAME;

class JwtGrantedAuthoritiesConverterTest {

    private static final String ACCESS_TOKEN = "access_token";

    @Mock
    private IdamRepository idamRepository;

    @InjectMocks
    private JwtGrantedAuthoritiesConverter converter;

    @Mock
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("No Claims should return empty authorities")
    void shouldReturnEmptyAuthoritiesWhenClaimNotAvailable() {
        when(jwt.hasClaim(TOKEN_NAME)).thenReturn(false);
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertEquals(0, authorities.size(), "size must be empty");
    }

    @Test
    @DisplayName("Should return empty authorities when claim value is not access_token")
    void shouldReturnEmptyAuthoritiesWhenClaimValueNotEquals() {
        when(jwt.hasClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn("Test");
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertEquals(0, authorities.size(), "size must be empty");
    }

    @Test
    @DisplayName("Should return empty authorities when roles are empty")
    void shouldReturnEmptyAuthoritiesWhenIdamReturnsNoUserRoles() {
        when(jwt.hasClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(ACCESS_TOKEN);
        UserInfo userInfo = mock(UserInfo.class);
        List<String> roles = new ArrayList<>();
        when(userInfo.getRoles()).thenReturn(roles);
        when(idamRepository.getUserInfo(BEARER + ACCESS_TOKEN)).thenReturn(userInfo);
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertEquals(0, authorities.size(), "size must be empty");
    }

    @Test
    @DisplayName("Should return empty authorities when roles are null")
    void shouldReturnEmptyAuthoritiesWhenIdamReturnsNullUserRoles() {
        when(jwt.hasClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(ACCESS_TOKEN);
        UserInfo userInfo = mock(UserInfo.class);
        List<String> roles = null;
        when(userInfo.getRoles()).thenReturn(roles);
        when(idamRepository.getUserInfo(BEARER + ACCESS_TOKEN)).thenReturn(userInfo);
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertEquals(0, authorities.size(), "size must be empty");
    }

    @Test
    @DisplayName("Should return authorities as per roles")
    void shouldReturnAuthoritiesWhenIdamReturnsUserRoles() {
        when(jwt.hasClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(ACCESS_TOKEN);
        UserInfo userInfo = mock(UserInfo.class);
        List<String> roles = new ArrayList<>();
        roles.add("citizen");
        when(userInfo.getRoles()).thenReturn(roles);
        when(idamRepository.getUserInfo(BEARER + ACCESS_TOKEN)).thenReturn(userInfo);
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assertEquals(1, authorities.size(), "should return one authority");
    }


    @Test
    @DisplayName("Should throw AuthenticationServiceException when IdamRepository error")
    void shouldThrowAuthenticationServiceExceptionWhenIdamRepositoryError() {

        // GIVEN
        when(jwt.hasClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(ACCESS_TOKEN);
        when(idamRepository.getUserInfo(BEARER + ACCESS_TOKEN)).thenThrow(new RuntimeException());

        // WHEN / THEN
        assertThatThrownBy(() -> converter.convert(jwt))
            .isInstanceOf(AuthenticationServiceException.class);
    }

    @Test
    void idamDiagnosticsAreLoggedOnlyAtDebug() {
        when(jwt.hasClaim(anyString())).thenReturn(true);
        when(jwt.getClaim(anyString())).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(ACCESS_TOKEN);
        when(idamRepository.getUserInfo(anyString())).thenThrow(new IllegalStateException("Synthetic IDAM detail"));
        Logger logger = (Logger) LoggerFactory.getLogger(JwtGrantedAuthoritiesConverter.class);
        Level previousLevel = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            logger.setLevel(Level.INFO);
            assertThrows(AuthenticationServiceException.class, () -> converter.convert(jwt));
            assertThat(appender.list).hasSize(1);
            assertThat(appender.list.get(0).getFormattedMessage()).isEqualTo("IDAM authentication failed");
            assertThat(appender.list.get(0).getThrowableProxy()).isNull();
            appender.list.clear();
            logger.setLevel(Level.DEBUG);
            assertThrows(AuthenticationServiceException.class, () -> converter.convert(jwt));
            assertThat(appender.list).hasSize(2);
            assertThat(appender.list.get(1).getLevel()).isEqualTo(Level.DEBUG);
            assertThat(appender.list.get(1).getThrowableProxy().getMessage()).isEqualTo("Synthetic IDAM detail");
        } finally {
            logger.setLevel(previousLevel);
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
