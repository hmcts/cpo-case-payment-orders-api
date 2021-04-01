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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.TestIdamConfiguration;
import uk.gov.hmcts.reform.cpo.ApplicationParams;
import uk.gov.hmcts.reform.cpo.config.SecurityConfiguration;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;


@SuppressWarnings({"PMD.ExcessiveImports"})
public class CasePaymentOrdersControllerTest {

    private static final String HTTP_ERROR_DETAILS_PATH = "$.details";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ApplicationParams applicationParams;


    private static final LocalDateTime EFFECTIVE_FROM = LocalDateTime.of(2021, Month.MARCH, 24, 11, 48, 32
    );
    private static final Long CASE_ID = 4_444_333_322_221_111L;
    private static final String ACTION = "action";
    private static final String RESPONSIBLE_PARTY = "responsibleParty";
    private static final String ORDER_REFERENCE = "orderReference";
    private static final UUID ID = UUID.randomUUID();
    private static final String CREATED_BY = "createdBy";
    private static final LocalDateTime CREATED_TIMESTAMP = LocalDateTime.now();

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
        protected CasePaymentOrdersServiceImpl casePaymentOrdersServiceImpl;

        @MockBean
        protected ApplicationParams applicationParams;

        @Autowired
        protected ObjectMapper objectMapper;

    }


    @SuppressWarnings({"PMD.TooManyMethods"})
    @Nested
    @DisplayName("PUT /case-payment-orders")
    class UpdateCasePaymentOrder extends BaseMvcTest {

        private UpdateCasePaymentOrderRequest request;
        private CasePaymentOrder casePaymentOrder;

        @BeforeEach
        void setUp() {
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            casePaymentOrder = CasePaymentOrder.builder()
                .caseId(CASE_ID)
                .effectiveFrom(EFFECTIVE_FROM)
                .action(ACTION)
                .responsibleParty(RESPONSIBLE_PARTY)
                .orderReference(ORDER_REFERENCE)
                .id(ID)
                .createdBy(CREATED_BY)
                .createdTimestamp(CREATED_TIMESTAMP)
                .build();

            given(casePaymentOrdersServiceImpl.updateCasePaymentOrder(any(UpdateCasePaymentOrderRequest.class)))
                .willReturn(casePaymentOrder);
        }

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() {

            // GIVEN
            CasePaymentOrdersController controller = new CasePaymentOrdersController(casePaymentOrdersServiceImpl,
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
                .andExpect(jsonPath("$.id", is(ID.toString())))
                .andExpect(jsonPath("$.created_timestamp", is(CREATED_TIMESTAMP.format(formatter))));

            // verify service call
            ArgumentCaptor<UpdateCasePaymentOrderRequest> captor =
                ArgumentCaptor.forClass(UpdateCasePaymentOrderRequest.class);
            verify(casePaymentOrdersServiceImpl).updateCasePaymentOrder(captor.capture());
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
                CASE_ID.toString(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.ID_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when ID is empty")
        @Test
        void shouldFailWithBadRequestWhenIdIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                "",
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.ID_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when ID is invalid")
        @Test
        void shouldFailWithBadRequestWhenIdIsInvalid() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                "INVALID",
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.ID_INVALID)));
        }

        @DisplayName("should fail with 400 bad request when Effective From is null")
        @Test
        void shouldFailWithBadRequestWhenEffectiveFromIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                null,
                CASE_ID.toString(),
                ACTION,
                RESPONSIBLE_PARTY,
                ORDER_REFERENCE
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.EFFECTIVE_FROM_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when Case ID is null")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                null,
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.CASE_ID_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when Case ID is empty")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                "",
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.CASE_ID_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when Case ID is invalid")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsInvalid() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                "INVALID",
                ORDER_REFERENCE,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.CASE_ID_INVALID)));
        }

        @DisplayName("should fail with 400 bad request when Order Reference is null")
        @Test
        void shouldFailWithBadRequestWhenOrderReferenceIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                null,
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.ORDER_REFERENCE_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when Order Reference is empty")
        @Test
        void shouldFailWithBadRequestWhenOrderReferenceIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                "",
                ACTION,
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.ORDER_REFERENCE_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when Action is null")
        @Test
        void shouldFailWithBadRequestWhenActionIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                ORDER_REFERENCE,
                null,
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.ACTION_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when Action is empty")
        @Test
        void shouldFailWithBadRequestWhenActionIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                ORDER_REFERENCE,
                "",
                RESPONSIBLE_PARTY
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.ACTION_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when Responsible Party is null")
        @Test
        void shouldFailWithBadRequestWhenResponsiblePartyIsNull() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                ORDER_REFERENCE,
                ACTION,
                null
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.RESPONSIBLE_PARTY_REQUIRED)));
        }

        @DisplayName("should fail with 400 bad request when Responsible Party is empty")
        @Test
        void shouldFailWithBadRequestWhenResponsiblePartyIsEmpty() throws Exception {

            // GIVEN
            request = new UpdateCasePaymentOrderRequest(
                ID.toString(),
                EFFECTIVE_FROM,
                CASE_ID.toString(),
                ORDER_REFERENCE,
                ACTION,
                ""
            );

            // WHEN
            this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(request)))
                // THEN
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasSize(1)))
                .andExpect(jsonPath(HTTP_ERROR_DETAILS_PATH, hasItem(ValidationError.RESPONSIBLE_PARTY_REQUIRED)));
        }

    }

}
