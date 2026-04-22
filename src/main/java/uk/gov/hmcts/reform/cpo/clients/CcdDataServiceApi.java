package uk.gov.hmcts.reform.cpo.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.cpo.security.SecurityUtils.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "ccd-client",
    url = "${ccd.data-store.url}"
)
public interface CcdDataServiceApi {
    String EXPERIMENTAL = "experimental=true";

    @GetMapping(
        path = "/cases/{cid}",
        headers = EXPERIMENTAL
    )
    void getCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("cid") String caseId
    );

}
