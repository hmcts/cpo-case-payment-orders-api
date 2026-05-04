package uk.gov.hmcts.reform.cpo.wiremock.extension;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicS2sLeaseResponseTransformer extends AbstractDynamicResponseTransformer {

    static final String DYNAMIC_S2S_LEASE_RESPONSE_TRANSFORMER = "dynamic-s2s-lease-response-transformer";
    private static final long TOKEN_TTL_MILLIS = 60 * 60 * 1000;
    private static final Pattern MICROSERVICE_PATTERN = Pattern.compile("\"microservice\"\\s*:\\s*\"([^\"]+)\"");

    @Override
    protected String dynamicResponse(Request request, Response response, Parameters parameters) {
        String requestBody = request.getBodyAsString();
        String microservice = extractMicroservice(requestBody);

        return Jwts.builder()
            .setSubject(microservice)
            .setExpiration(new Date(System.currentTimeMillis() + TOKEN_TTL_MILLIS))
            .signWith(SignatureAlgorithm.HS256, Keys.secretKeyFor(SignatureAlgorithm.HS256))
            .compact();
    }

    @Override
    public String getName() {
        return DYNAMIC_S2S_LEASE_RESPONSE_TRANSFORMER;
    }

    private String extractMicroservice(String requestBody) {
        Matcher matcher = MICROSERVICE_PATTERN.matcher(requestBody);
        return matcher.find() ? matcher.group(1) : "unknown_service";
    }
}
