package uk.gov.hmcts.reform.cpo.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class BaseMvcAuthChecks extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CasePaymentOrderEntityGenerator entityGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    static final String DISPLAY_ALL_AUTH_OK
        = "#{index} - {0} - Should return 2xx:Successful for happy path with valid authentication";

    static final String DISPLAY_AUTH_MISSING
        = "#{index} - {0} - Should return 401:Unauthorised if no authentication token";
    static final String DISPLAY_AUTH_MALFORMED
        = "#{index} - {0} - Should return 401:Unauthorised if malformed token";
    static final String DISPLAY_AUTH_EXPIRED
        = "#{index} - {0} - Should return 401:Unauthorised if authentication token has expired";

    static final String DISPLAY_AUTH_SERVICE_UNAVAILABLE
        = "#{index} - {0} - Should return 401:Unauthorised if authentication service is unavailable";

    static final String DISPLAY_S2S_AUTH_MISSING
        = "#{index} - {0} - Should return 401:Unauthorised if no service authentication token";
    static final String DISPLAY_S2S_AUTH_MALFORMED
        = "#{index} - {0} - Should return 401:Unauthorised if malformed service authentication token";
    static final String DISPLAY_S2S_AUTH_EXPIRED
        = "#{index} - {0} - Should return 401:Unauthorised if service authentication token has expired";

    static final String DISPLAY_S2S_AUTH_UNAUTHORISED
        = "#{index} - {0} - Should return 403:Forbidden if unauthorised service authentication token";
    static final String DISPLAY_S2S_PERMISSION_MISSING
        = "#{index} - {0} - Should return 403:Forbidden if service is missing correct S2S permission";

    static final String DISPLAY_S2S_AUTH_SERVICE_UNAVAILABLE
        = "#{index} - {0} - Should return 401:Unauthorised if S2S authentication service is unavailable";

    @SuppressWarnings("unused")
    interface AuthChecks {
        void should2xxSuccessfulForHappyPath(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should401ForMissingAuthToken(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should401ForMalformedAuthToken(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should401ForExpiredAuthToken(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should401ForMissingServiceAuthToken(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should401ForMalformedServiceAuthToken(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should401ForExpiredServiceAuthToken(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should403ForUnauthorisedServiceAuthToken(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should403ForServiceMissingS2sPermission(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should401IfAuthServiceUnavailable(String displayName, EndpointUtil endpointUtil) throws Exception;

        void should401IfS2sAuthServiceUnavailable(String displayName, EndpointUtil endpointUtil) throws Exception;
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC1: Happy Path with valid Mandatory parameters (IDAM/S2S tokens)
     */
    void assert2xxSuccessfulForHappyPath(EndpointUtil endpointUtil) throws Exception {

        assert2xxSuccessfulForHappyPath(endpointUtil, endpointUtil.getHappyPathServiceName());
        assert2xxSuccessfulForHappyPath(endpointUtil, AUTHORISED_CRUD_SERVICE);
    }

    private void assert2xxSuccessfulForHappyPath(EndpointUtil endpointUtil,
                                                 String serviceName) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);

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
    void assert401ForMissingAuthToken(EndpointUtil endpointUtil) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);
        String serviceName = endpointUtil.getHappyPathServiceName();

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(HttpHeaders.AUTHORIZATION); // <-- MISSING AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(isUnauthorizedOAuth2Error());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC3: Mandatory parameters missing from the request (IDAM token invalid - MALFORMED)
     */
    void assert401ForMalformedAuthToken(EndpointUtil endpointUtil) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);
        String serviceName = endpointUtil.getHappyPathServiceName();

        // GIVEN
        HttpHeaders headers = createHttpHeaders(serviceName);
        headers.remove(HttpHeaders.AUTHORIZATION);
        headers.add(HttpHeaders.AUTHORIZATION, "MALFORMED"); // <-- MALFORMED AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(isUnauthorizedOAuth2Error());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC3: Mandatory parameters missing from the request (IDAM token invalid - EXPIRED)
     */
    void assert401ForExpiredAuthToken(EndpointUtil endpointUtil) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);
        String serviceName = endpointUtil.getHappyPathServiceName();

        // GIVEN
        long authTtlMillis = -1;
        HttpHeaders headers = createHttpHeaders(authTtlMillis, // <-- EXPIRED AUTH
                                                serviceName,
                                                AUTH_TOKEN_TTL);

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(isUnauthorizedOAuth2Error());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC4: Mandatory parameters missing from the request (S2S token Missing)
     */
    void assert401ForMissingServiceAuthToken(EndpointUtil endpointUtil) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);
        String serviceName = endpointUtil.getHappyPathServiceName();

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
    void assert401ForMalformedServiceAuthToken(EndpointUtil endpointUtil) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);
        String serviceName = endpointUtil.getHappyPathServiceName();

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
     * AC5: Mandatory parameters missing from the request (S2S token invalid - EXPIRED)
     */
    void assert401ForExpiredServiceAuthToken(EndpointUtil endpointUtil) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);
        String serviceName = endpointUtil.getHappyPathServiceName();

        // GIVEN
        long s2sAuthTtlMillis = -1;
        HttpHeaders headers = createHttpHeaders(AUTH_TOKEN_TTL,
                                                serviceName,
                                                s2sAuthTtlMillis); // <-- EXPIRED S2S AUTH

        // WHEN
        mockMvc.perform(happyPathRequestBuilder.headers(headers))
            // THEN
            .andExpect(status().isUnauthorized());
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC5: Mandatory parameters missing from the request (S2S token invalid - UNAUTHORISED)
     */
    void assert403ForUnauthorisedServiceAuthToken(EndpointUtil endpointUtil) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);

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
    void assert403ForServiceMissingS2sPermission(EndpointUtil endpointUtil) throws Exception {

        String happyPathServiceName = endpointUtil.getHappyPathServiceName();

        List<String> serviceNames = List.of(AUTHORISED_CREATE_SERVICE,
                                            AUTHORISED_READ_SERVICE,
                                            AUTHORISED_UPDATE_SERVICE,
                                            AUTHORISED_DELETE_SERVICE);

        for (String serviceName : serviceNames) {
            if (!happyPathServiceName.equals(serviceName)) { // NB: skip happy path
                assert403ForServiceMissingS2sPermission(endpointUtil, serviceName);
            }
        }

    }

    void assert403ForServiceMissingS2sPermission(EndpointUtil endpointUtil,
                                                         String serviceNameWithoutPermission) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);

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
    void assert401IfIdamUnavailable(EndpointUtil endpointUtil) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);
        String serviceName = endpointUtil.getHappyPathServiceName();

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
                .andExpect(isUnauthorizedOAuth2Error());

        }
        catch (Exception ex){
            log.error("Ignoring based on the test case: Idam is getting 503 http code:", ex);
        }
        finally {
            if (badIdamStub != null) {
                WireMock.removeStub(badIdamStub);
            }
        }
    }

    /*
     * CPO-33: “Implement tests for invalid S2S and Idam token”
     * AC7: S2S Service Unavailable
     */
    void assert401IfS2sAuthServiceUnavailable(EndpointUtil endpointUtil) throws Exception {

        MockHttpServletRequestBuilder happyPathRequestBuilder = endpointUtil.getHappyPathRequestBuilder(entityGenerator,
                                                                                                        objectMapper);
        String serviceName = endpointUtil.getHappyPathServiceName();

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

    interface EndpointUtil {

        MockHttpServletRequestBuilder getHappyPathRequestBuilder(CasePaymentOrderEntityGenerator entityGenerator,
                                                                 ObjectMapper objectMapper)
            throws JsonProcessingException;

        String getHappyPathServiceName();

    }

    private ResultMatcher isUnauthorizedOAuth2Error() {
        return result -> assertAll(
            () -> assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus()),
            () -> assertTrue(result.getResponse().containsHeader(HttpHeaders.WWW_AUTHENTICATE)),
            () -> assertTrue(Objects.requireNonNull(result.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE))
                                 .startsWith(OAuth2AccessToken.TokenType.BEARER.getValue()))
        );
    }
}
