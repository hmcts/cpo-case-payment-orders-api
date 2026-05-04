package uk.gov.hmcts.reform.cpo.service.impl;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.cpo.clients.CcdDataServiceApi;
import uk.gov.hmcts.reform.cpo.service.CaseAccessClient;

@Component
@Slf4j
public class CaseAccessClientImpl implements CaseAccessClient {

    private final AuthTokenGenerator authTokenGenerator;
    private final CcdDataServiceApi ccdDataServiceApi;

    public CaseAccessClientImpl(AuthTokenGenerator authTokenGenerator,
                                CcdDataServiceApi ccdDataServiceApi) {
        this.authTokenGenerator = authTokenGenerator;
        this.ccdDataServiceApi = ccdDataServiceApi;
    }

    @Override
    public void assertCanAccessCase(String userToken, String caseId) {
        try {
            ccdDataServiceApi.getCase(userToken, authTokenGenerator.generate(), caseId);
        } catch (FeignException ex) {
            if (ex.status() == 403 || ex.status() == 404) {
                throw new AccessDeniedException("User does not have access to case: " + caseId);
            }
            throw ex;
        } catch (Exception ex) {
            throw ex;
        }
    }
}
