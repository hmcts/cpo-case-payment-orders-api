package uk.gov.hmcts.reform.cpo.security;

import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.cpo.exception.UnauthorisedServiceException;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class SecurityUtils {

    public static final String BEARER = "Bearer ";

    private final IdamRepository idamRepository;
    private final List<String> readOnlyServices;

    @Autowired
    public SecurityUtils(IdamRepository idamRepository,
                         @Value("${idam.s2s-authorised.read-only-services}") List<String> readOnlyServices) {
        this.idamRepository = idamRepository;
        this.readOnlyServices = readOnlyServices;
    }

    public String getUserToken() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return BEARER + jwt.getTokenValue();
    }

    public UserInfo getUserInfo() {
        return idamRepository.getUserInfo(getUserToken());
    }

    public String getServiceNameFromS2SToken(String serviceAuthenticationToken) {
        // NB: this grabs the service name straight from the token under the assumption
        // that the S2S token has already been verified elsewhere
        return JWT.decode(removeBearerFromToken(serviceAuthenticationToken)).getSubject();
    }

    private String removeBearerFromToken(String token) {
        return token.startsWith(BEARER) ? token.substring(BEARER.length()) : token;
    }

    public void isReadOnlyService(String token) {
        if (isBlank(token)) {
            throw new UnauthorisedServiceException("The provided S2S token is missing or invalid");
        }

        String serviceName = getServiceNameFromS2SToken(token);

        if (readOnlyServices.contains(serviceName)) {
            throw new UnauthorisedServiceException(
                "Service does not possess the required CRUD permissions to access this resource");
        }
    }
}
