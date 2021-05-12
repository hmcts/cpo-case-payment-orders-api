package uk.gov.hmcts.reform.cpo.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersAuditJpaRepository;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersJpaRepository;
import uk.gov.hmcts.reform.cpo.utils.CasePaymentOrderEntityGenerator;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.IDS;

class CasePaymentOrdersControllerAuthChecksIT extends BaseMvcAuthChecks implements BaseMvcAuthChecks.AuthChecks {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CasePaymentOrdersAuditJpaRepository casePaymentOrdersAuditJpaRepository;

    @Autowired
    private CasePaymentOrdersJpaRepository casePaymentOrdersJpaRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        casePaymentOrdersJpaRepository.deleteAllInBatch();
        casePaymentOrdersAuditJpaRepository.deleteAllInBatch();
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_ALL_AUTH_OK)
    public void should2xxSuccessfulForHappyPath(String displayName,
                                                EndpointUtil endpointUtil) throws Exception {
        assert2xxSuccessfulForHappyPath(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_AUTH_MISSING)
    public void should401ForMissingAuthToken(String displayName,
                                             EndpointUtil endpointUtil) throws Exception {
        assert401ForMissingAuthToken(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_AUTH_MALFORMED)
    public void should401ForMalformedAuthToken(String displayName,
                                               EndpointUtil endpointUtil) throws Exception {
        assert401ForMalformedAuthToken(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_AUTH_EXPIRED)
    public void should401ForExpiredAuthToken(String displayName,
                                             EndpointUtil endpointUtil) throws Exception {
        assert401ForExpiredAuthToken(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_S2S_AUTH_MISSING)
    public void should401ForMissingServiceAuthToken(String displayName,
                                                    EndpointUtil endpointUtil) throws Exception {
        assert401ForMissingServiceAuthToken(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_S2S_AUTH_MALFORMED)
    public void should401ForMalformedServiceAuthToken(String displayName,
                                                      EndpointUtil endpointUtil) throws Exception {
        assert401ForMalformedServiceAuthToken(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_S2S_AUTH_EXPIRED)
    public void should401ForExpiredServiceAuthToken(String displayName,
                                                    EndpointUtil endpointUtil) throws Exception {
        assert401ForExpiredServiceAuthToken(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_S2S_AUTH_UNAUTHORISED)
    public void should403ForUnauthorisedServiceAuthToken(String displayName,
                                                         EndpointUtil endpointUtil) throws Exception {
        assert403ForUnauthorisedServiceAuthToken(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_S2S_PERMISSION_MISSING)
    public void should403ForServiceMissingS2sPermission(String displayName,
                                                        EndpointUtil endpointUtil) throws Exception {
        assert403ForServiceMissingS2sPermission(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_AUTH_SERVICE_UNAVAILABLE)
    public void should401IfAuthServiceUnavailable(String displayName,
                                                  EndpointUtil endpointUtil) throws Exception {
        assert401IfIdamUnavailable(endpointUtil);
    }

    @Override
    @MethodSource("getTestEndpoints")
    @ParameterizedTest(name = DISPLAY_S2S_AUTH_SERVICE_UNAVAILABLE)
    public void should401IfS2sAuthServiceUnavailable(String displayName,
                                                     EndpointUtil endpointUtil) throws Exception {
        assert401IfS2sAuthServiceUnavailable(endpointUtil);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getTestEndpoints() {
        return Stream.of(
            Arguments.arguments("POST /case-payment-orders", new CreateCasePaymentOrderEndpointUtil()),
            Arguments.arguments("GET /case-payment-orders", new GetCasePaymentOrderEndpointUtil()),
            Arguments.arguments("PUT /case-payment-orders", new UpdateCasePaymentOrderEndpointUtil()),
            Arguments.arguments("DELETE /case-payment-orders", new DeleteCasePaymentOrderEndpointUtil())
        );
    }

    static class CreateCasePaymentOrderEndpointUtil implements BaseMvcAuthChecks.EndpointUtil {

        @Override
        public MockHttpServletRequestBuilder getHappyPathRequestBuilder(CasePaymentOrderEntityGenerator entityGenerator,
                                                                        ObjectMapper objectMapper)
            throws JsonProcessingException {

            CreateCasePaymentOrderRequest createCasePaymentOrderRequest =
                new CreateCasePaymentOrderRequest(entityGenerator.generateUniqueCaseId(),
                                                  ACTION,
                                                  RESPONSIBLE_PARTY,
                                                  ORDER_REFERENCE_VALID);

            return post(CASE_PAYMENT_ORDERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest));
        }

        @Override
        public String getHappyPathServiceName() {
            return AUTHORISED_CREATE_SERVICE;
        }

    }

    static class GetCasePaymentOrderEndpointUtil implements BaseMvcAuthChecks.EndpointUtil {

        @Override
        public MockHttpServletRequestBuilder getHappyPathRequestBuilder(CasePaymentOrderEntityGenerator entityGenerator,
                                                                        ObjectMapper objectMapper) {

            CasePaymentOrderEntity savedEntity = entityGenerator.generateAndSaveEntities(1).get(0);

            return get(CASE_PAYMENT_ORDERS_PATH)
                .queryParam(IDS, savedEntity.getId().toString());
        }

        @Override
        public String getHappyPathServiceName() {
            return AUTHORISED_READ_SERVICE;
        }

    }

    static class UpdateCasePaymentOrderEndpointUtil implements BaseMvcAuthChecks.EndpointUtil {

        @Override
        public MockHttpServletRequestBuilder getHappyPathRequestBuilder(CasePaymentOrderEntityGenerator entityGenerator,
                                                                        ObjectMapper objectMapper)
            throws JsonProcessingException {

            CasePaymentOrderEntity originalEntity = entityGenerator.generateAndSaveEntities(1).get(0);

            UpdateCasePaymentOrderRequest request = new UpdateCasePaymentOrderRequest(
                originalEntity.getId().toString(),
                entityGenerator.generateUniqueCaseId(),
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            return put(CASE_PAYMENT_ORDERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
        }

        @Override
        public String getHappyPathServiceName() {
            return AUTHORISED_UPDATE_SERVICE;
        }

    }

    static class DeleteCasePaymentOrderEndpointUtil implements BaseMvcAuthChecks.EndpointUtil {

        @Override
        public MockHttpServletRequestBuilder getHappyPathRequestBuilder(CasePaymentOrderEntityGenerator entityGenerator,
                                                                        ObjectMapper objectMapper) {
            CasePaymentOrderEntity savedEntity = entityGenerator.generateAndSaveEntities(1).get(0);

            return delete(CASE_PAYMENT_ORDERS_PATH)
                .queryParam(IDS, savedEntity.getId().toString());
        }

        @Override
        public String getHappyPathServiceName() {
            return AUTHORISED_DELETE_SERVICE;
        }

    }

}
