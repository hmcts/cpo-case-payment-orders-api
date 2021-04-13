package uk.gov.hmcts.reform.cpo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.TestIdamConfiguration;
import uk.gov.hmcts.reform.cpo.ApplicationParams;
import uk.gov.hmcts.reform.cpo.config.SecurityConfiguration;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersQueryException;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.validation.ConstraintViolationException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_IDS;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.IDS;


public class CasePaymentOrdersControllerTest implements BaseTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @WebMvcTest(controllers = CasePaymentOrdersController.class,
        includeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = MapperConfig.class),
        excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
            {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class}))
    @AutoConfigureMockMvc(addFilters = false)
    @ImportAutoConfiguration(TestIdamConfiguration.class)
    static class BaseMvcTest {

        @Autowired
        protected MockMvc mockMvc;

        @MockBean
        protected CasePaymentOrdersService casePaymentOrdersService;

        @MockBean
        protected ApplicationParams applicationParams;

        @Autowired
        protected ObjectMapper objectMapper;
    }

    @Nested
    @DisplayName("PUT /case-payment-orders")
    class UpdateCasePaymentOrder extends BaseMvcTest {

        private UpdateCasePaymentOrderRequest request;
        private CasePaymentOrder casePaymentOrder;

        @BeforeEach
        void setUp() {
            request = createUpdateCasePaymentOrderRequest();

            casePaymentOrder = createCasePaymentOrder();

            given(casePaymentOrdersService.updateCasePaymentOrder(any(UpdateCasePaymentOrderRequest.class)))
                .willReturn(casePaymentOrder);
        }

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() {

            // GIVEN
            CasePaymentOrdersController controller = new CasePaymentOrdersController(casePaymentOrdersService,
                                                                                     applicationParams);

            // WHEN
            CasePaymentOrder response = controller.updateCasePaymentOrderRequest(request);

            // THEN
            assertThat(response).isNotNull().isEqualTo(casePaymentOrder);
        }

        @DisplayName("should delegate to service for a valid request")
        @Test
        void shouldSuccessfullyCreateCasePaymentOrder() throws Exception {

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id", is(CPO_ID_VALID_1)))
                .andExpect(jsonPath("$.created_timestamp", is(CREATED_TIMESTAMP.format(formatter))));

            // verify service call
            ArgumentCaptor<UpdateCasePaymentOrderRequest> captor =
                ArgumentCaptor.forClass(UpdateCasePaymentOrderRequest.class);
            verify(casePaymentOrdersService).updateCasePaymentOrder(captor.capture());
            assertEquals("Service called with request data: ID",
                         request.getId(), captor.getValue().getId());
            assertEquals("Service called with request data: Effective from",
                         request.getEffectiveFrom(), captor.getValue().getEffectiveFrom());
            assertEquals("Service called with request data: Case ID",
                         request.getCaseId(), captor.getValue().getCaseId());
            assertEquals("Service called with request data: Action",
                         request.getAction(), captor.getValue().getAction());
            assertEquals("Service called with request data: ResponsibleParty",
                         request.getResponsibleParty(), captor.getValue().getResponsibleParty());
            assertEquals("Service called with request data: Order reference",
                         request.getOrderReference(), captor.getValue().getOrderReference());

        }

        @DisplayName("should fail with 400 bad request when ID is null")
        @Test
        void shouldFailWithBadRequestWhenIdIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                null,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when ID is empty")
        @Test
        void shouldFailWithBadRequestWhenIdIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                "",
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when ID is invalid")
        @Test
        void shouldFailWithBadRequestWhenIdIsInvalid() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_INVALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_INVALID);
        }

        @DisplayName("should fail with 400 bad request when Effective From is null")
        @Test
        void shouldFailWithBadRequestWhenEffectiveFromIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                null,
                CASE_ID_VALID_1,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.EFFECTIVE_FROM_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Case ID is null")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                null,
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Case ID is empty")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                "",
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Case ID is invalid")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsInvalid() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_INVALID_LUHN,
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_INVALID);
        }

        @DisplayName("should fail with 400 bad request when Order Reference is null")
        @Test
        void shouldFailWithBadRequestWhenOrderReferenceIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                null,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ORDER_REFERENCE_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Order Reference is empty")
        @Test
        void shouldFailWithBadRequestWhenOrderReferenceIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                "",
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ORDER_REFERENCE_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Action is null")
        @Test
        void shouldFailWithBadRequestWhenActionIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ORDER_REFERENCE,
                null,
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ACTION_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Action is empty")
        @Test
        void shouldFailWithBadRequestWhenActionIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ORDER_REFERENCE,
                "",
                RESPONSIBLE_PARTY
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ACTION_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Responsible Party is null")
        @Test
        void shouldFailWithBadRequestWhenResponsiblePartyIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ORDER_REFERENCE,
                ACTION,
                null
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.RESPONSIBLE_PARTY_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Responsible Party is empty")
        @Test
        void shouldFailWithBadRequestWhenResponsiblePartyIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ORDER_REFERENCE,
                ACTION,
                ""
            );

            // WHEN
            ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content(objectMapper.writeValueAsString(request)));

            // THEN
            assertBadRequestResponse(result, ValidationError.RESPONSIBLE_PARTY_REQUIRED);
        }

    }

    private void assertBadRequestResponse(ResultActions result,
                                          String validationDetails) throws Exception {

        result
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
            .andExpect(jsonPath("$.details", hasSize(1)))
            .andExpect(jsonPath("$.details", hasItem(validationDetails)));
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders?id=")
    class DeleteCasePaymentOrdersById extends BaseMvcTest {

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() throws Exception {
            CasePaymentOrdersController controller
                    = new CasePaymentOrdersController(casePaymentOrdersService, applicationParams);

            controller.deleteCasePaymentOrdersById(Optional.of(List.of(UUID.randomUUID().toString())),
                    Optional.of(List.of()));
        }

        @DisplayName("should delete case payment order specified by id")
        @Test
        void shouldDeleteCasePaymentOrderById() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).param(IDS, UUID.randomUUID().toString()))
                    .andExpect(status().isOk());
        }

        @DisplayName("should Fail With 400 BadRequest When Id Is Not a UUID")
        @Test
        void shouldFailWithBadRequestWhenIdIsNotUUID() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).param(IDS, "123"))
                    .andExpect(status().isBadRequest());
        }

        @DisplayName("should Fail With 400 BadRequest When IDs are mix of valid and invalid UUIDs")
        @Test
        void shouldFailWithBadRequestWhenIdListContainsValidAndInvalidUUID() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                    .param(IDS, UUID.randomUUID().toString(), "123", UUID.randomUUID().toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders?case-ids=")
    class DeleteCasePaymentOrdersByCaseId extends BaseMvcTest {

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() {
            CasePaymentOrdersController controller
                    = new CasePaymentOrdersController(casePaymentOrdersService, applicationParams);
            controller.deleteCasePaymentOrdersById(Optional.of(Collections.emptyList()),
                    Optional.of(List.of(CASE_ID_VALID_1)));
        }

        @DisplayName("should delete case payment order specified by case id")
        @Test
        void shouldDeleteCasePaymentOrderById() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).param(CASE_IDS, CASE_ID_VALID_1))
                    .andExpect(status().isOk());
        }

        @DisplayName("should Fail With 400 BadRequest When Case Id Is Not a valid length Case Id")
        @Test
        void shouldFailWithBadRequestWhenIdIsNotValidLengthCaseId() throws Exception {
            doThrow(ConstraintViolationException.class).when(casePaymentOrdersService).deleteCasePaymentOrders(any());
            this.mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).param(CASE_IDS, "123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("deleteCasePaymentOrdersById.caseIds: "
                            + "These caseIDs: 123 are incorrect")));
        }

        @DisplayName("should Fail With 400 BadRequest When Case Id Is Not a valid Luhn")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsNotValidLuhn() throws Exception {
            //doThrow(ConstraintViolationException.class).when(casePaymentOrdersService).deleteCasePaymentOrders(any());
            this.mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH).param(CASE_IDS, CASE_ID_INVALID_LUHN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(
                            containsString("deleteCasePaymentOrdersById.caseIds: These caseIDs: "
                                    + CASE_ID_INVALID_LUHN
                                    + " are incorrect.")));
        }

        @DisplayName("should Fail With 400 BadRequest When IDs are mix of valid and invalid LUHNs")
        @Test
        void shouldFailWithBadRequestWhenIdListContainsValidAndInvalidCaseIds() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                    .param(CASE_IDS, CASE_ID_VALID_1, CASE_ID_INVALID_LUHN, CASE_ID_VALID_2))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("deleteCasePaymentOrdersById.caseIds: These caseIDs: "
                            + CASE_ID_INVALID_LUHN
                            + " are incorrect.")));
        }
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders")
    class DeleteCasePaymentOrdersInvalidParameters extends BaseMvcTest {

        @DisplayName("should return 200 OK when neither the optional id and case id request parameters are specified")
        @Test
        void shouldReturn200OkWhenNoOptionalRequestParametersAreSpecified() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());
        }

        @DisplayName("should Fail With 4xx when both ids and case-ids request parameter are specified")
        @Test
        void shouldFailWhenIdAndCaseIdRequestParameterIsSpecified() throws Exception {
            doThrow(CasePaymentOrdersQueryException.class)
                    .when(casePaymentOrdersService).deleteCasePaymentOrders(any());
            this.mockMvc.perform(delete(CASE_PAYMENT_ORDERS_PATH)
                    .param(IDS, UUID.randomUUID().toString(), UUID.randomUUID().toString())
                    .param(CASE_IDS, CASE_ID_VALID_1, CASE_ID_VALID_2))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().is4xxClientError());
        }
    }
}
