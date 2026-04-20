package uk.gov.hmcts.reform.cpo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.cpo.service.CaseAccessClient;

@Component
@Slf4j
public class CaseAccessClientImpl implements CaseAccessClient {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestClient restClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final String ccdDataStoreUrl;

    public CaseAccessClientImpl(RestClient.Builder restClientBuilder,
                                AuthTokenGenerator authTokenGenerator,
                                @Value("${ccd.data-store.url}") String ccdDataStoreUrl) {
        this.restClient = restClientBuilder.build();
        this.authTokenGenerator = authTokenGenerator;
        this.ccdDataStoreUrl = ccdDataStoreUrl;
    }

    @Override
    public void assertCanAccessCase(String userToken, String caseId) {
        log.info("Checking CCD access for caseId={} via ccdDataStoreUrl={}", caseId, ccdDataStoreUrl);
        String serviceToken = authTokenGenerator.generate();

        String rawServiceToken = serviceToken.startsWith("Bearer ")
            ? serviceToken.substring("Bearer ".length())
            : serviceToken;

        String serviceSub = null;
        try {
            serviceSub = com.auth0.jwt.JWT.decode(rawServiceToken).getSubject();
        } catch (Exception e) {
            log.warn("Unable to decode generated service token subject", e);
        }

        String userSub = null;
        try {
            String rawUserToken = userToken.startsWith("Bearer ")
                ? userToken.substring("Bearer ".length())
                : userToken;
            userSub = com.auth0.jwt.JWT.decode(rawUserToken).getSubject();
        } catch (Exception e) {
            log.warn("Unable to decode user token subject", e);
        }

        log.info("Checking CCD access for caseId={} via ccdDataStoreUrl={} userSub={} serviceSub={}",
                 caseId, ccdDataStoreUrl, userSub, serviceSub);
        try {
            restClient.get()
                .uri(ccdDataStoreUrl + "/cases/{caseId}", caseId)
                .header("Authorization", userToken)
                .header(SERVICE_AUTHORIZATION, serviceToken)
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.NotFound ex) {
            log.warn("CCD access check failed for caseId={} status={} body={}",
                     caseId,
                     ex.getStatusCode(),
                     ex.getResponseBodyAsString());
            log.warn("Access denied when checking case access for case {}", caseId, ex);
            throw new AccessDeniedException("User does not have access to case: " + caseId);
        } catch (RestClientException ex) {
            log.error("Error while checking access to case {}", caseId, ex);
            throw ex;
        }

    }
}
