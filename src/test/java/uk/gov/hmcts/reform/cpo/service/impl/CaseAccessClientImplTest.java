package uk.gov.hmcts.reform.cpo.service.impl;

import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.cpo.clients.CcdDataServiceApi;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CaseAccessClientImplTest {

    private static final String USER_TOKEN = "Bearer user-token";
    private static final String SERVICE_TOKEN = "Bearer service-token";
    private static final String CASE_ID = "1234567890123456";

    private AuthTokenGenerator authTokenGenerator;
    private CcdDataServiceApi ccdDataServiceApi;
    private CaseAccessClientImpl client;

    @BeforeEach
    void setUp() {
        authTokenGenerator = mock(AuthTokenGenerator.class);
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        ccdDataServiceApi = mock(CcdDataServiceApi.class);
        client = new CaseAccessClientImpl(authTokenGenerator, ccdDataServiceApi);
    }

    @Test
    void shouldCallCcdWithUserAndServiceAuthorizationHeaders() {
        client.assertCanAccessCase(USER_TOKEN, CASE_ID);

        verify(authTokenGenerator).generate();
        verify(ccdDataServiceApi).getCase(USER_TOKEN, SERVICE_TOKEN, CASE_ID);
    }

    @Test
    void shouldDenyAccessWhenCcdReturns403() {
        doThrow(feignException(403)).when(ccdDataServiceApi).getCase(USER_TOKEN, SERVICE_TOKEN, CASE_ID);

        assertThatThrownBy(() -> client.assertCanAccessCase(USER_TOKEN, CASE_ID))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("User does not have access to case: " + CASE_ID);
    }

    @Test
    void shouldDenyAccessWhenCcdReturns404() {
        doThrow(feignException(404)).when(ccdDataServiceApi).getCase(USER_TOKEN, SERVICE_TOKEN, CASE_ID);

        assertThatThrownBy(() -> client.assertCanAccessCase(USER_TOKEN, CASE_ID))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("User does not have access to case: " + CASE_ID);
    }

    @Test
    void shouldRethrowUnexpectedFeignErrors() {
        doThrow(feignException(500)).when(ccdDataServiceApi).getCase(USER_TOKEN, SERVICE_TOKEN, CASE_ID);

        assertThatThrownBy(() -> client.assertCanAccessCase(USER_TOKEN, CASE_ID))
            .isInstanceOf(FeignException.class);
    }

    private static FeignException feignException(int status) {
        Request request = Request.create(HttpMethod.GET, "/cases/" + CASE_ID, Map.of(), null, null, null);
        Response response = Response.builder()
            .status(status)
            .reason("error")
            .request(request)
            .headers(Map.of())
            .body("error", StandardCharsets.UTF_8)
            .build();
        return FeignException.errorStatus("getCase", response);
    }
}
