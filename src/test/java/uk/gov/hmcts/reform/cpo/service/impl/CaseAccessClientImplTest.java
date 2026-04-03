package uk.gov.hmcts.reform.cpo.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withForbiddenRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class CaseAccessClientImplTest {

    private static final String USER_TOKEN = "Bearer user-token";
    private static final String SERVICE_TOKEN = "Bearer service-token";
    private static final String CASE_ID = "1234567890123456";
    private static final String CCD_URL = "http://ccd-data-store";

    private AuthTokenGenerator authTokenGenerator;
    private MockRestServiceServer server;
    private CaseAccessClientImpl client;

    @BeforeEach
    void setUp() {
        authTokenGenerator = mock(AuthTokenGenerator.class);
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        client = new CaseAccessClientImpl(restClientBuilder, authTokenGenerator, CCD_URL);
    }

    @Test
    @DisplayName("should call CCD with user and service authorization headers")
    void shouldCallCcdWithUserAndServiceAuthorizationHeaders() {
        server.expect(requestTo(CCD_URL + "/cases/" + CASE_ID))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", USER_TOKEN))
            .andExpect(header("ServiceAuthorization", SERVICE_TOKEN))
            .andRespond(withNoContent());

        client.assertCanAccessCase(USER_TOKEN, CASE_ID);
        verify(authTokenGenerator).generate();
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {403, 404})
    @DisplayName("should deny access when CCD rejects the case access check")
    void shouldDenyAccessWhenCcdRejectsTheCaseAccessCheck(int httpStatus) {
        server.expect(requestTo(CCD_URL + "/cases/" + CASE_ID))
            .andRespond(httpStatus == 403 ? withForbiddenRequest() : withResourceNotFound());

        assertThatThrownBy(() -> client.assertCanAccessCase(USER_TOKEN, CASE_ID))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("User does not have access to case: " + CASE_ID);

        verify(authTokenGenerator).generate();
        server.verify();
    }

    @Test
    @DisplayName("should rethrow CCD client errors that are not authorization failures")
    void shouldRethrowCcdClientErrorsThatAreNotAuthorizationFailures() {

        server.expect(requestTo(CCD_URL + "/cases/" + CASE_ID))
            .andRespond(withServerError());

        assertThatThrownBy(() -> client.assertCanAccessCase(USER_TOKEN, CASE_ID))
            .isInstanceOf(RestClientResponseException.class);



        server.verify();
    }
}
