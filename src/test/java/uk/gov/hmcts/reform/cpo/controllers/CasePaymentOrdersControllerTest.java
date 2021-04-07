package uk.gov.hmcts.reform.cpo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.is;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@SuppressWarnings({"PMD.ExcessiveImports"})
public class CasePaymentOrdersControllerTest {

    private static final LocalDateTime EFFECTIVE_FROM = LocalDateTime.of(2021, Month.MARCH, 24,
                                                                         11, 48, 32
    );
    private static final Long CASE_ID = 6_551_341_964_128_977L;
    private static final String ACTION = "action";
    private static final String RESPONSIBLE_PARTY = "responsibleParty";
    private static final String ORDER_REFERENCE = "2021-11223344556";
    private static final UUID ID = UUID.randomUUID();
    private static final String CREATED_BY = "createdBy";
    private static final LocalDateTime CREATED_TIMESTAMP = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @WebMvcTest(controllers = CasePaymentOrdersController.class,
        includeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = MapperConfig.class),
        excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
            {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class, CasePaymentOrdersService.class}))
    @AutoConfigureMockMvc(addFilters = false)
    @ImportAutoConfiguration(TestIdamConfiguration.class)
    static class BaseMvcTest {

        @Autowired
        protected MockMvc mockMvc;

        @MockBean
        protected CasePaymentOrdersService casePaymentOrdersService;

        @Autowired
        protected ObjectMapper objectMapper;

        @MockBean
        ApplicationParams applicationParams;

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
            CasePaymentOrdersController controller = new CasePaymentOrdersController(casePaymentOrdersService,
                                                                                     applicationParams);
            CasePaymentOrder response = controller.createCasePaymentOrderRequest(createCasePaymentOrderRequest);
            assertThat(response).isNotNull();
            assertEquals(response, casePaymentOrder);

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
}
