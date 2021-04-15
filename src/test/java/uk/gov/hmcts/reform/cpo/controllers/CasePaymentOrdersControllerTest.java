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
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.TestIdamConfiguration;
import uk.gov.hmcts.reform.cpo.ApplicationParams;
import uk.gov.hmcts.reform.cpo.config.SecurityConfiguration;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;

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
    @DisplayName("POST /case-payment-orders")
    class CreateCasePaymentOrder extends BaseMvcTest {

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequest;
        private CasePaymentOrder casePaymentOrder;

        @BeforeEach
        void setUp() {

            // create payload and domain model from same sample data in `BaseTest`
            createCasePaymentOrderRequest = createCreateCasePaymentOrderRequest();
            casePaymentOrder = createCasePaymentOrder();

        }

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() {
            given(casePaymentOrdersService.createCasePaymentOrder(createCasePaymentOrderRequest))
                .willReturn(casePaymentOrder);
            CasePaymentOrdersController controller = new CasePaymentOrdersController(
                casePaymentOrdersService,
                applicationParams
            );
            CasePaymentOrder response = controller.createCasePaymentOrderRequest(createCasePaymentOrderRequest);
            assertThat(response).isNotNull();
            assertEquals("Case Payment Order returned", response, casePaymentOrder);

        }


        @DisplayName("happy path test with mockMvc")
        @Test
        void shouldSuccessfullyCreateCasePaymentOrder() throws Exception {
            given(casePaymentOrdersService.createCasePaymentOrder(any(CreateCasePaymentOrderRequest.class)))
                .willReturn(casePaymentOrder);

            this.mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id", is(casePaymentOrder.getId().toString())))
                .andExpect(jsonPath("$.created_timestamp", is(CREATED_TIMESTAMP.format(formatter))));
        }

    }


    @Nested
    @DisplayName("PUT /case-payment-orders")
    class UpdateCasePaymentOrder extends BaseMvcTest {

        private UpdateCasePaymentOrderRequest updateCasePaymentOrderRequest;
        private CasePaymentOrder casePaymentOrder;

        @BeforeEach
        void setUp() {

            // create payload and domain model from same sample data in `BaseTest`
            updateCasePaymentOrderRequest = createUpdateCasePaymentOrderRequest();
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
            CasePaymentOrder response = controller.updateCasePaymentOrderRequest(updateCasePaymentOrderRequest);

            // THEN
            assertThat(response).isNotNull().isEqualTo(casePaymentOrder);
        }

        @DisplayName("should delegate to service for a valid request")
        @Test
        void shouldSuccessfullyUpdateCasePaymentOrder() throws Exception {

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)))
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
                         updateCasePaymentOrderRequest.getId(), captor.getValue().getId());
            assertEquals("Service called with request data: Effective from",
                         updateCasePaymentOrderRequest.getEffectiveFrom(), captor.getValue().getEffectiveFrom());
            assertEquals("Service called with request data: Case ID",
                         updateCasePaymentOrderRequest.getCaseId(), captor.getValue().getCaseId());
            assertEquals("Service called with request data: Action",
                         updateCasePaymentOrderRequest.getAction(), captor.getValue().getAction());
            assertEquals("Service called with request data: ResponsibleParty",
                         updateCasePaymentOrderRequest.getResponsibleParty(), captor.getValue().getResponsibleParty());
            assertEquals("Service called with request data: Order reference",
                         updateCasePaymentOrderRequest.getOrderReference(), captor.getValue().getOrderReference());

        }

        @DisplayName("should fail with 400 bad request when ID is null")
        @Test
        void shouldFailWithBadRequestWhenIdIsNull() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                null,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when ID is empty")
        @Test
        void shouldFailWithBadRequestWhenIdIsEmpty() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                "",
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when ID is invalid")
        @Test
        void shouldFailWithBadRequestWhenIdIsInvalid() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_INVALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ID_INVALID);
        }

        @DisplayName("should fail with 400 bad request when Effective From is null")
        @Test
        void shouldFailWithBadRequestWhenEffectiveFromIsNull() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                null,
                CASE_ID_VALID_1,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.EFFECTIVE_FROM_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Case ID is null")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsNull() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                null,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Case ID is empty")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsEmpty() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                "",
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Case ID is invalid")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsInvalid() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_INVALID_LUHN,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.CASE_ID_INVALID);
        }

        @DisplayName("should fail with 400 bad request when Action is null")
        @Test
        void shouldFailWithBadRequestWhenActionIsNull() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                null,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ACTION_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Action is empty")
        @Test
        void shouldFailWithBadRequestWhenActionIsEmpty() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                "",
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ACTION_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Responsible Party is null")
        @Test
        void shouldFailWithBadRequestWhenResponsiblePartyIsNull() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ACTION,
                null,
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.RESPONSIBLE_PARTY_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Responsible Party is empty")
        @Test
        void shouldFailWithBadRequestWhenResponsiblePartyIsEmpty() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ACTION,
                "",
                ORDER_REFERENCE_VALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.RESPONSIBLE_PARTY_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Order Reference is null")
        @Test
        void shouldFailWithBadRequestWhenOrderReferenceIsNull() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ACTION,
                RESPONSIBLE_PARTY,
                null
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ORDER_REFERENCE_REQUIRED);
        }

        @DisplayName("should fail with 400 bad request when Order Reference is empty")
        @Test
        void shouldFailWithBadRequestWhenOrderReferenceIsEmpty() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ACTION,
                RESPONSIBLE_PARTY,
                ""
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ORDER_REFERENCE_INVALID);
        }

        @DisplayName("should fail with 400 bad request when Order Reference is invalid")
        @Test
        void shouldFailWithBadRequestWhenOrderReferenceIsInvalid() throws Exception {

            // GIVEN
            updateCasePaymentOrderRequest = new UpdateCasePaymentOrderRequest(
                CPO_ID_VALID_1,
                EFFECTIVE_FROM,
                CASE_ID_VALID_1,
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE_INVALID
            );

            // WHEN
            ResultActions result =
                this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .content(objectMapper.writeValueAsString(updateCasePaymentOrderRequest)));

            // THEN
            assertBadRequestResponse(result, ValidationError.ORDER_REFERENCE_INVALID);
        }

    }

    private void assertBadRequestResponse(ResultActions result,
                                          String validationDetails) throws Exception {

        result
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_PATH_STATUS).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_PATH_ERROR).value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
            .andExpect(jsonPath(ERROR_PATH_DETAILS, hasSize(1)))
            .andExpect(jsonPath(ERROR_PATH_DETAILS, hasItem(validationDetails)));
    }

}
