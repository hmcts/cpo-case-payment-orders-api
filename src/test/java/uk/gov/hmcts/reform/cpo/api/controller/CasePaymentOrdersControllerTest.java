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
import uk.gov.hmcts.reform.TestIdamConfiguration;
import uk.gov.hmcts.reform.cpo.config.SecurityConfiguration;
import uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController;
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

        @Autowired
        protected ObjectMapper objectMapper;
    }

    @Nested
    @DisplayName("DELETE /case-payment-orders?id=")
    // @SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.JUnitTestsShouldIncludeAssert", "PMD.ExcessiveImports"})
    class DeleteCasePaymentOrdersById extends BaseMvcTest {

        private static final String IDS = "ids";

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() { // created to avoid IDE warnings in controller class that function is never used
            // ARRANGE
            //given(service.assignCaseAccess(any(CaseAssignment.class))).willReturn(roles);

            CasePaymentOrdersController controller = new CasePaymentOrdersController();

            // ACT
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
    @DisplayName("DELETE /case-payment-orders?case-id=")
    // @SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.JUnitTestsShouldIncludeAssert", "PMD.ExcessiveImports"})
    class DeleteCasePaymentOrdersByCaseId extends BaseMvcTest {

        private static final String CASE_IDS = "case-ids";

        private static final String VALID_LUHN_1 = "9511425043588823";
        private static final String VALID_LUHN_2 = "9716401307140455";
        private static final String INVALID_LUHN = "9653285214520123";

        @DisplayName("happy path test without mockMvc")
        @Test
        void directCallHappyPath() { // created to avoid IDE warnings in controller class that function is never used
            // ARRANGE
            //given(service.assignCaseAccess(any(CaseAssignment.class))).willReturn(roles);

            CasePaymentOrdersController controller = new CasePaymentOrdersController();

            // ACT
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

        @DisplayName("should Fail With 4xx no request parameter is specified")
        @Test
        void shouldFailWhenNoRequestParameterIsSpecified() throws Exception {
            this.mockMvc.perform(delete(CASE_PAYMENTS_ORDER_PATH))
                    .andExpect(status().is4xxClientError());
        }
    }
}
