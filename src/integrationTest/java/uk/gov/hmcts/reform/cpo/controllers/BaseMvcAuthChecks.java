package uk.gov.hmcts.reform.cpo.controllers;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.BaseTest.UNAUTHORISED_SERVICE;
import static uk.gov.hmcts.reform.cpo.BaseTest.createHttpHeaders;

interface BaseMvcAuthChecks {

    String DISPLAY_ALL_AUTH_OK = "Should return 2xx:successful for happy path with valid authentication";

    String DISPLAY_AUTH_MISSING = "Should return 401:unauthorised if no authentication token";
    String DISPLAY_AUTH_MALFORMED = "Should return 401:unauthorised if malformed token";
    String DISPLAY_AUTH_EXPIRED = "Should return 401:unauthorised if authentication token has expired";

    String DISPLAY_AUTH_SERVICE_UNAVAILABLE
        = "Should return 401:unauthorised if authentication service is unavailable";

    String DISPLAY_S2S_AUTH_MISSING = "Should return 401:unauthorised if no service authentication token";
    String DISPLAY_S2S_AUTH_MALFORMED = "Should return 401:unauthorised if malformed service authentication token";
    String DISPLAY_S2S_AUTH_UNAUTHORISED = "Should return 403:forbidden if unauthorised service authentication token";

    String DISPLAY_S2S_PERMISSION_MISSING
        = "Should return 403:forbidden if service is missing correct S2S permission";

    String DISPLAY_S2S_AUTH_SERVICE_UNAVAILABLE
        = "Should return 401:unauthorised if S2S authentication service is unavailable";

    void should2xxSuccessfulForHappyPath() throws Exception;

    void should401ForMissingAuthToken() throws Exception;

    void should401ForMalformedAuthToken() throws Exception;

    void should401ForExpiredAuthToken() throws Exception;

    void should401ForMissingServiceAuthToken() throws Exception;

    void should401ForMalformedServiceAuthToken() throws Exception;

    void should403ForUnauthorisedServiceAuthToken() throws Exception;

    void should403ForServiceMissingS2sPermission() throws Exception;

    void should401IfAuthServiceUnavailable() throws Exception;

    void should401IfS2sAuthServiceUnavailable() throws Exception;

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC1: Happy Path with valid Mandatory parameters (IDAM/S2S tokens)
     */
    default void assert2xxSuccessfulForHappyPath(MockMvc mockMvc,
                                                 MockHttpServletRequestBuilder happyPathRequestBuilder,
                                                 String serviceName) throws Exception {
        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName); // <-- VALID AUTH & S2S AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().is2xxSuccessful());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC2: Mandatory parameters missing from the request (IDAM token Missing)
     */
    default void assert401ForMissingAuthToken(MockMvc mockMvc,
                                              MockHttpServletRequestBuilder happyPathRequestBuilder,
                                              String serviceName) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(HttpHeaders.AUTHORIZATION); // <-- MISSING AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC3: Mandatory parameters missing from the request (IDAM token invalid - MALFORMED)
     */
    default void assert401ForMalformedAuthToken(MockMvc mockMvc,
                                                MockHttpServletRequestBuilder happyPathRequestBuilder,
                                                String serviceName) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(HttpHeaders.AUTHORIZATION);
        headers.add(HttpHeaders.AUTHORIZATION, "MALFORMED"); // <-- MALFORMED AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC3: Mandatory parameters missing from the request (IDAM token invalid - EXPIRED)
     */
    default void assert401ForExpiredAuthToken(MockMvc mockMvc,
                                               MockHttpServletRequestBuilder happyPathRequestBuilder,
                                               String serviceName) throws Exception {

        // GIVEN
        long authTtlMillis = -1;
        HttpHeaders headers = createHttpHeaders(serviceName, authTtlMillis); // <-- EXPIRED AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC4: Mandatory parameters missing from the request (S2S token Missing)
     */
    default void assert401ForMissingServiceAuthToken(MockMvc mockMvc,
                                                     MockHttpServletRequestBuilder happyPathRequestBuilder,
                                                     String serviceName) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(SecurityUtils.SERVICE_AUTHORIZATION); // <-- MISSING S2S AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC5: Mandatory parameters missing from the request (S2S token invalid - MALFORMED)
     */
    default void assert401ForMalformedServiceAuthToken(MockMvc mockMvc,
                                                       MockHttpServletRequestBuilder happyPathRequestBuilder,
                                                       String serviceName) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(SecurityUtils.SERVICE_AUTHORIZATION);
        headers.add(SecurityUtils.SERVICE_AUTHORIZATION, "MALFORMED"); // <-- MALFORMED S2S AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC5: Mandatory parameters missing from the request (S2S token invalid - UNAUTHORISED)
     */
    default void assert403ForUnauthorisedServiceAuthToken(MockMvc mockMvc,
                                                          MockHttpServletRequestBuilder happyPathRequestBuilder
    ) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(UNAUTHORISED_SERVICE); // <-- UNAUTHORISED S2S AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isForbidden());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC5: Mandatory parameters missing from the request (S2S token invalid - MISSING-PERMISSION)
     */
    default void assert403ForServiceMissingS2sPermission(MockMvc mockMvc,
                                                         MockHttpServletRequestBuilder happyPathRequestBuilder,
                                                         String serviceNameWithoutPermission
    ) throws Exception {

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceNameWithoutPermission); // <-- MISSING-PERMISSION

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isForbidden());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC6: IDAM Service Unavailable
     */
    default void assert401IfIdamUnavailable(MockMvc mockMvc,
                                            MockHttpServletRequestBuilder happyPathRequestBuilder,
                                            String serviceName) throws Exception {
        StubMapping badIdamStub = null;

        try {

            // GIVEN
            HttpHeaders headers = createHttpHeaders(serviceName); // <-- VALID
            // change IDAM mock to ServiceUnavailable
            badIdamStub = WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/o/userinfo"))
                                               .willReturn(WireMock.serviceUnavailable()));

            // WHEN
            mockMvc.perform(happyPathRequestBuilder.headers(headers))
                // THEN
                .andExpect(status().isUnauthorized());

        } finally {
            if (badIdamStub != null) {
                WireMock.removeStub(badIdamStub);
            }
        }
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC7: S2S Service Unavailable
     */
    default void assert401IfS2sAuthServiceUnavailable(MockMvc mockMvc,
                                                      MockHttpServletRequestBuilder happyPathRequestBuilder,
                                                      String serviceName) throws Exception {
        StubMapping badS2sStub = null;

        try {

            // GIVEN
            HttpHeaders headers = createHttpHeaders(serviceName); // <-- VALID
            // change S2S Auth Service to ServiceUnavailable
            badS2sStub = WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/s2s/details"))
                                              .willReturn(WireMock.serviceUnavailable()));

            // WHEN
            mockMvc.perform(happyPathRequestBuilder.headers(headers))
                // THEN
                .andExpect(status().isUnauthorized());

        } finally {
            if (badS2sStub != null) {
                WireMock.removeStub(badS2sStub);
            }
        }
    }

}
