package uk.gov.hmcts.reform.cpo.security;

import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import jakarta.servlet.http.HttpServletRequest;

import static uk.gov.hmcts.reform.cpo.security.Permission.CREATE;
import static uk.gov.hmcts.reform.cpo.security.Permission.DELETE;
import static uk.gov.hmcts.reform.cpo.security.Permission.READ;
import static uk.gov.hmcts.reform.cpo.security.Permission.UPDATE;

@Service
@Slf4j
public class SecurityUtils {

    public static final String SERVICE_AUTHORIZATION = ServiceAuthFilter.AUTHORISATION;

    public static final String BEARER = JwtGrantedAuthoritiesConverter.BEARER;

    private final IdamRepository idamRepository;
    private final ServiceAuthorizationConfig serviceAuthorizationConfig;

    @Autowired
    public SecurityUtils(IdamRepository idamRepository, ServiceAuthorizationConfig serviceAuthorizationConfig) {
        this.idamRepository = idamRepository;
        this.serviceAuthorizationConfig = serviceAuthorizationConfig;
    }

    public String getUserToken() {
        var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return BEARER + jwt.getTokenValue();
    }

    public UserInfo getUserInfo() {
        return idamRepository.getUserInfo(getUserToken());
    }

    public String getServiceNameFromS2SToken(String serviceAuthenticationToken) {
        String token = removeBearerFromToken(serviceAuthenticationToken);

        // NB: this grabs the service name straight from the token under the assumption
        // that the S2S token has already been verified elsewhere
        return token != null ? JWT.decode(token).getSubject() : null;
    }

    private String removeBearerFromToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return token.startsWith(BEARER) ? token.substring(BEARER.length()) : token;
    }

    public boolean hasCreatePermission() {
        return hasPermission(CREATE);
    }

    public boolean hasReadPermission() {
        return hasPermission(READ);
    }

    public boolean hasUpdatePermission() {
        return hasPermission(UPDATE);
    }

    public boolean hasDeletePermission() {
        return hasPermission(DELETE);
    }

    private boolean hasPermission(Permission permission) {
        var requestAttributes = RequestContextHolder.getRequestAttributes();

        String serviceAuthorizationHeaderValue = null;
        if (requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            serviceAuthorizationHeaderValue = request.getHeader(SERVICE_AUTHORIZATION);
        }

        if (serviceAuthorizationHeaderValue == null) {
            log.warn(ValidationError.SERVICE_AUTHORIZATION_MISSING);
            return false;
        }

        String serviceName = getServiceNameFromS2SToken(serviceAuthorizationHeaderValue);

        return serviceAuthorizationConfig.hasPermissions(serviceName, permission);
    }
}
