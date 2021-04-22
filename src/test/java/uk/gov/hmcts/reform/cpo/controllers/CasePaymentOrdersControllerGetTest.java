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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.TestIdamConfiguration;
import uk.gov.hmcts.reform.cpo.config.SecurityConfiguration;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;


public class CasePaymentOrdersControllerGetTest implements BaseTest {

    private static final LocalDateTime EFFECTIVE_FROM = LocalDateTime.of(2021, Month.MARCH, 24,
                                                                         11, 48, 32
    );
    private static final Long CASE_ID = 6_551_341_964_128_977L;
    private static final String ORDER_REFERENCE = "2021-11223344556";
    private static final UUID ID = UUID.randomUUID();


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

            createCasePaymentOrderRequest = new CreateCasePaymentOrderRequest(EFFECTIVE_FROM, "6551341964128977",
                                                                              ACTION, RESPONSIBLE_PARTY,
                                                                              ORDER_REFERENCE
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

        }

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() {
            given(casePaymentOrdersService.createCasePaymentOrder(createCasePaymentOrderRequest))
                .willReturn(casePaymentOrder);
            CasePaymentOrdersController controller = new CasePaymentOrdersController(
                casePaymentOrdersService
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

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

            this.mockMvc.perform(post(CASE_PAYMENT_ORDERS_PATH)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id", is(ID.toString())))
                .andExpect(jsonPath("$.created_timestamp", is(CREATED_TIMESTAMP.format(formatter))));
        }

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
            CasePaymentOrdersController controller = new CasePaymentOrdersController(
                casePaymentOrdersService
            );

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
                         request.getId(), captor.getValue().getId()
            );
            assertEquals("Service called with request data: Effective from",
                         request.getEffectiveFrom(), captor.getValue().getEffectiveFrom()
            );
            assertEquals("Service called with request data: Case ID",
                         request.getCaseId(), captor.getValue().getCaseId()
            );
            assertEquals("Service called with request data: Action",
                         request.getAction(), captor.getValue().getAction()
            );
            assertEquals("Service called with request data: ResponsibleParty",
                         request.getResponsibleParty(), captor.getValue().getResponsibleParty()
            );
            assertEquals("Service called with request data: Order reference",
                         request.getOrderReference(), captor.getValue().getOrderReference()
            );

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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
            ResultActions result = this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
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
    @DisplayName("Get /case-payment-orders")
    class GetCasePaymentOrder extends BaseMvcTest {

        private CasePaymentOrderQueryFilter casePaymentOrderQueryFilter;
        private CasePaymentOrder casePaymentOrder;

        private final List<String> casesIds = createInitialValuesList(new String[]{"1609243447569251",
            "1609243447569252", "1609243447569253"}).get();

        private final List<String> ids = createInitialValuesList(new String[]{"df54651b-3227-4067-9f23-6ffb32e2c6bd",
            "d702ef36-0ca7-46e9-8a00-ef044d78453e",
            "d702ef36-0ca7-46e9-8a00-ef044d78453e"}).get();


        @BeforeEach
        void setUp() {
            casePaymentOrder = createCasePaymentOrder();
        }

        @DisplayName("happy path for ids")
        @Test
        void passPathForIds() {

            casePaymentOrderQueryFilter = getACasePaymentOrderQueryFilter(PAGE_SIZE, Collections.emptyList(), ids);
            final Page<CasePaymentOrder> getDomainPages = getDomainPages();

            when(casePaymentOrdersService.getCasePaymentOrders(any(CasePaymentOrderQueryFilter.class)))
                .thenReturn(getDomainPages);

            // GIVEN
            CasePaymentOrdersController controller = new CasePaymentOrdersController(
                casePaymentOrdersService
            );

            // WHEN
            Page<CasePaymentOrder> response = controller.getCasePaymentOrders(
                Optional.of(ids),
                Optional.empty(),
                getPageRequest()
            );

            // THEN
            assertTrue(
                "The total of expected elements should be" + PAGE_SIZE,
                response.getContent().size() == PAGE_SIZE
            );

            assertArrayEquals(response.getContent().toArray(), getDomainPages.getContent().toArray());
        }

        @DisplayName("happy path for case-ids")
        @Test
        void passForCasesIds() {

            casePaymentOrderQueryFilter = getACasePaymentOrderQueryFilter(PAGE_SIZE, casesIds, Collections.emptyList());
            final Page<CasePaymentOrder> getDomainPages = getDomainPages();

            when(casePaymentOrdersService.getCasePaymentOrders(any(CasePaymentOrderQueryFilter.class)))
                .thenReturn(getDomainPages);

            // GIVEN
            CasePaymentOrdersController controller = new CasePaymentOrdersController(
                casePaymentOrdersService
            );

            // WHEN
            Page<CasePaymentOrder> response = controller.getCasePaymentOrders(
                Optional.empty(),
                Optional.of(casesIds),
                getPageRequest()
            );

            // THEN
            assertTrue(
                "The total of expected elements should be" + PAGE_SIZE,
                response.getContent().size() == PAGE_SIZE
            );

            assertArrayEquals(response.getContent().toArray(), getDomainPages.getContent().toArray());
        }


        @DisplayName("fail for for case-ids and ids")
        @Test
        void failForCasesAndIds() {

            // GIVEN
            CasePaymentOrdersController controller = new CasePaymentOrdersController(
                casePaymentOrdersService
            );

            // WHEN
            try {
                ResultActions response = this.mockMvc.perform(
                    request(HttpMethod.GET, CASE_PAYMENT_ORDERS_PATH)
                        .param(IDS, "b00445ee-5bed-42c5-812f-12687175beca")
                        .param(CASE_IDS, "1574419234651640,1574932009200070")
                );
                // THEN
                assertGetCopPResponse(ValidationError.CPO_FILER_ERROR, response);
            } catch (Exception exception) {
                exception.printStackTrace();
                fail(UN_EXPECTED_ERROR_IN_TEST);
            }

        }

        @DisplayName("fail for for ids only")
        @Test
        void failForIds() {
            final String expectedError =
                "getCasePaymentOrders.ids: These ids: XXXX are incorrect.";
            // GIVEN
            CasePaymentOrdersController controller = new CasePaymentOrdersController(
                casePaymentOrdersService
            );

            // WHEN
            try {
                ResultActions response = this.mockMvc.perform(
                    request(HttpMethod.GET, CASE_PAYMENT_ORDERS_PATH)
                        .param(IDS, "XXXX")
                );
                // THEN
                assertGetCopPResponse(expectedError, response);
            } catch (Exception exception) {
                exception.printStackTrace();
                fail(UN_EXPECTED_ERROR_IN_TEST);
            }

        }

        @DisplayName("fail for for case-ids only")
        @Test
        void failForCaseIds() {
            final String expectedError =
                "Case ID has to be a valid 16-digit Luhn number.";
            // GIVEN
            CasePaymentOrdersController controller = new CasePaymentOrdersController(
                casePaymentOrdersService
            );

            // WHEN
            try {
                ResultActions response = this.mockMvc.perform(
                    request(HttpMethod.GET, CASE_PAYMENT_ORDERS_PATH)
                        .param(CASE_IDS, "XXXX")
                );
                // THEN
                assertGetCopPResponse(expectedError, response);
            } catch (Exception exception) {
                exception.printStackTrace();
                fail(UN_EXPECTED_ERROR_IN_TEST);
            }

        }
    }
}
