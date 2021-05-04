package uk.gov.hmcts.reform.cpo.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.BaseTest.UNAUTHORISED_SERVICE;
import static uk.gov.hmcts.reform.cpo.BaseTest.createHttpHeaders;

interface BaseMvcAuthChecks {

    String DISPLAY_AUTH_MISSING = "Should return 401:unauthorised if no authentication token";
    String DISPLAY_AUTH_MALFORMED = "Should return 401:unauthorised if malformed token";
    String DISPLAY_AUTH_EXPIRED = "Should return 401:unauthorised if authentication token has expired";

    String DISPLAY_S2S_AUTH_MISSING = "Should return 401:unauthorised if no service authentication token";
    String DISPLAY_S2S_AUTH_MALFORMED = "Should return 401:unauthorised if malformed service authentication token";
    String DISPLAY_S2S_AUTH_UNAUTHORISED = "Should return 403:forbidden if unauthorised service authentication token";

    void should401ForMissingAuthToken() throws Exception;

    void should401ForMalformedAuthToken() throws Exception;

    void should401ForExpiredAuthToken() throws Exception;

    void should401ForMissingServiceAuthToken() throws Exception;

    void should401ForMalformedServiceAuthToken() throws Exception;

    void should403ForUnauthorisedServiceAuthToken() throws Exception;

    default void assert401ForMissingAuthToken(MockMvc mockMvc,
                                               MockHttpServletRequestBuilder happyPathRequestBuilder,
                                               String serviceName) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(HttpHeaders.AUTHORIZATION); // <-- MISSING

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    default void assert401ForMalformedAuthToken(MockMvc mockMvc,
                                                 MockHttpServletRequestBuilder happyPathRequestBuilder,
                                                 String serviceName) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(HttpHeaders.AUTHORIZATION);
        headers.add(HttpHeaders.AUTHORIZATION, "MALFORMED"); // <-- MALFORMED

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    default void assert401ForExpiredAuthToken(MockMvc mockMvc,
                                               MockHttpServletRequestBuilder happyPathRequestBuilder,
                                               String serviceName) throws Exception {

        // GIVEN
        long authTtlMillis = -1;
        HttpHeaders headers = createHttpHeaders(serviceName, authTtlMillis); // <-- EXPIRED

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    default void assert401ForMissingServiceAuthToken(MockMvc mockMvc,
                                                      MockHttpServletRequestBuilder happyPathRequestBuilder,
                                                      String serviceName) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(SecurityUtils.SERVICE_AUTHORIZATION); // <-- MISSING

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    default void assert401ForMalformedServiceAuthToken(MockMvc mockMvc,
                                                        MockHttpServletRequestBuilder happyPathRequestBuilder,
                                                        String serviceName) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(SecurityUtils.SERVICE_AUTHORIZATION);
        headers.add(SecurityUtils.SERVICE_AUTHORIZATION, "MALFORMED"); // <-- MALFORMED

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    default void assert403ForUnauthorisedServiceAuthToken(MockMvc mockMvc,
                                                           MockHttpServletRequestBuilder happyPathRequestBuilder
    ) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(UNAUTHORISED_SERVICE); // <-- UNAUTHORISED

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isForbidden());
    }

}
