package uk.gov.hmcts.reform.cpo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.reform.TestIdamConfiguration;
import uk.gov.hmcts.reform.cpo.ApplicationParams;
import uk.gov.hmcts.reform.cpo.config.SecurityConfiguration;
import uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController;
import uk.gov.hmcts.reform.cpo.errorhandling.CasePaymentIdentifierException;
import uk.gov.hmcts.reform.cpo.errorhandling.ValidationError;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CasePaymentOrdersControllerTest {

    private static final String CASE_PAYMENTS_ORDER_PATH = "/case-payment-orders";
    private static final String CASE_IDS = "case-ids";
    private static final String IDS = "ids";

    private static final String VALID_LUHN_1 = "9511425043588823";
    private static final String VALID_LUHN_2 = "9716401307140455";
    private static final String INVALID_LUHN = "9653285214520123";

    @WebMvcTest(controllers = CasePaymentOrdersController.class,
            includeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = CasePaymentOrderMapper.class),
            excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
                    { SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class }))
    @AutoConfigureMockMvc(addFilters = false)
    @ImportAutoConfiguration(TestIdamConfiguration.class)
    static class BaseMvcTest {

        @Autowired
        protected MockMvc mockMvc;

        @MockBean
        protected CasePaymentOrdersService service;

        @MockBean
        protected ApplicationParams applicationParams;

        @Autowired
        protected ObjectMapper objectMapper;
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders?id=")
    class DeleteCasePaymentOrdersById extends BaseMvcTest {

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() throws Exception {
            CasePaymentOrdersController controller = new CasePaymentOrdersController(service, applicationParams);

            controller.deleteCasePaymentOrdersById(List.of(UUID.randomUUID()));
        }

        @DisplayName("should delete case payment order specified by id")
        @Test
        void shouldDeleteCasePaymentOrderById() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH).param(IDS, UUID.randomUUID().toString()))
                    .andExpect(status().isOk());
        }

        @DisplayName("should Fail With 400 BadRequest When Id Is Null")
        @Test
        void shouldFailWithBadRequestWhenIdIsNull() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH).param(IDS, ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString(ValidationError.IDS_EMPTY)));
        }

        @DisplayName("should Fail With 400 BadRequest When Id Is Not a UUID")
        @Test
        void shouldFailWithBadRequestWhenIdIsNotUUID() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH).param(IDS, "123"))
                    .andExpect(status().isBadRequest());
        }

        @DisplayName("should Fail With 400 BadRequest When IDs are mix of valid and invalid UUIDs")
        @Test
        void shouldFailWithBadRequestWhenIdListContainsValidAndInvalidUUID() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH)
                    .param(IDS, UUID.randomUUID().toString(), "123", UUID.randomUUID().toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders?case-ids=")
    class DeleteCasePaymentOrdersByCaseId extends BaseMvcTest {

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() throws CasePaymentIdentifierException {
            CasePaymentOrdersController controller = new CasePaymentOrdersController(service, applicationParams);
            controller.deleteCasePaymentOrdersByCaseId(List.of(VALID_LUHN_1));
        }

        @DisplayName("should delete case payment order specified by case id")
        @Test
        void shouldDeleteCasePaymentOrderById() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH).param(CASE_IDS, VALID_LUHN_1))
                    .andExpect(status().isOk());
        }

        @DisplayName("should Fail With 400 BadRequest When Case Ids is Empty")
        @Test
        void shouldFailWithBadRequestWhenIdIsNull() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH).param(CASE_IDS, ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString(ValidationError.CASE_IDS_EMPTY)));
        }

        @DisplayName("should Fail With 400 BadRequest When Case Id Is Not a valid length Case Id")
        @Test
        void shouldFailWithBadRequestWhenIdIsNotValidLengthCaseId() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH).param(CASE_IDS, "123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString(ValidationError.CASE_ID_INVALID_LENGTH)));
        }

        @DisplayName("should Fail With 400 BadRequest When Case Id Is Not a valid Luhn")
        @Test
        void shouldFailWithBadRequestWhenCaseIdIsNotValidLuhn() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH).param(CASE_IDS, INVALID_LUHN))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString(ValidationError.CASE_IDS_INVALID)));
        }

        @DisplayName("should Fail With 400 BadRequest When IDs are mix of valid and invalid LUHNs")
        @Test
        void shouldFailWithBadRequestWhenIdListContainsValidAndInvalidCaseIds() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH)
                    .param(CASE_IDS, VALID_LUHN_1, INVALID_LUHN, VALID_LUHN_2))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString(ValidationError.CASE_IDS_INVALID)));
        }
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders")
    class DeleteCasePaymentOrdersInvalidParameters extends BaseMvcTest {

        @DisplayName("should Fail With 4xx no request parameter is specified")
        @Test
        void shouldFailWhenNoRequestParameterIsSpecified() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().is4xxClientError());
        }

        @DisplayName("should Fail With 4xx when both ids and case-ids request parameter are specified")
        @Test
        void shouldFailWhenIdAndCaseIdRequestParameterIsSpecified() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH)
                    .param(IDS, UUID.randomUUID().toString(), UUID.randomUUID().toString())
                    .param(CASE_IDS, VALID_LUHN_1, VALID_LUHN_2))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().is4xxClientError());
        }
    }

}
