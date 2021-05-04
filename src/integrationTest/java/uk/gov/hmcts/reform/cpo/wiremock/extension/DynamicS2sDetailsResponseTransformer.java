package uk.gov.hmcts.reform.cpo.wiremock.extension;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/*
 * Replaces response body with service name read from supplied token before returning the response
 */
public class DynamicS2sDetailsResponseTransformer extends AbstractDynamicResponseTransformer {

    static final String DYNAMIC_S2S_DETAILS_RESPONSE_TRANSFORMER = "dynamic-s2s-details-response-transformer";

    private final SecurityUtils securityUtils;

    public DynamicS2sDetailsResponseTransformer(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @Override
    protected String dynamicResponse(Request request, Response response, Parameters parameters) {
        String s2sToken = request.getHeader(AUTHORIZATION);

        return securityUtils.getServiceNameFromS2SToken(s2sToken);
    }

    @Override
    public String getName() {
        return DYNAMIC_S2S_DETAILS_RESPONSE_TRANSFORMER;
    }

}
