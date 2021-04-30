package uk.gov.hmcts.reform.cpo.security;

import com.auth0.jwt.JWT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
public class SecurityUtils {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    public static final String BEARER = "Bearer ";

    private final IdamRepository idamRepository;

    @Autowired
    public SecurityUtils(IdamRepository idamRepository) {
        this.idamRepository = idamRepository;
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

}
