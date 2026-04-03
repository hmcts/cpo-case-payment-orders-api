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
        try{
            restClient.get()
                .uri(ccdDataStoreUrl + "/cases/{caseId}", caseId)
                .header("Authorization", userToken)
                .header(SERVICE_AUTHORIZATION, authTokenGenerator.generate())
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.NotFound ex) {
            log.warn("Access denied when checking case access for case {}", caseId, ex);
            throw new AccessDeniedException("User does not have access to case: " + caseId);
        } catch (RestClientException ex) {
            log.error("Error while checking access to case {}", caseId, ex);
            throw ex;
        }

    }
}
