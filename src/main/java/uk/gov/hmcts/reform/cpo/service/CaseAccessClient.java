package uk.gov.hmcts.reform.cpo.service;

public interface CaseAccessClient {

    void assertCanAccessCase(String userToken, String caseId);
}
