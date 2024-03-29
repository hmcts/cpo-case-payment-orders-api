package uk.gov.hmcts.reform;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public interface BaseTest {

    String ERROR_PATH_DETAILS = "$.details";
    String ERROR_PATH_ERROR = "$.error";
    String ERROR_PATH_MESSAGE = "$.message";
    String ERROR_PATH_STATUS = "$.status";

    int PAGE_NUMBER = 1;
    int PAGE_SIZE = 3;

    String CASE_ID_VALID_1 = "9511425043588823";
    String CASE_ID_VALID_2 = "9716401307140455";
    String CASE_ID_VALID_3 = "4444333322221111";
    String CASE_ID_INVALID_NON_NUMERIC = "NON_NUMERIC";
    String CASE_ID_INVALID_LUHN = "9653285214520123"; // NB: correct length: 16 digits
    String CASE_ID_INVALID_LENGTH = "3038"; // NB: valid luhn number

    String CPO_ID_VALID_1 = "df54651b-3227-4067-9f23-6ffb32e2c6bd";
    String CPO_ID_VALID_2 = "d702ef36-0ca7-46e9-8a00-ef044d78453e";
    String CPO_ID_VALID_3 = "d702ef36-0ca7-46e9-8a00-ef044d78453e";
    String CPO_ID_INVALID_NON_NUMERIC = "NON_NUMERIC";
    String CPO_ID_INVALID_1 = "160924";
    String CPO_ID_INVALID_2 = "160924 ";

    String ORDER_REFERENCE_VALID = "2021-1122334455667";
    String ORDER_REFERENCE_INVALID = "2021-918425346";
    String ACTION = "action";
    String RESPONSIBLE_PARTY = "responsibleParty";

    String REQUEST_ID = "Test Request ID";

    String CREATED_BY = "createdBy";
    LocalDateTime CREATED_TIMESTAMP = LocalDateTime.now();
    boolean HISTORY_EXISTS_DEFAULT = false;
    boolean HISTORY_EXISTS_UPDATED = true;

    default <T> Optional<List<T>> createInitialValuesList(final T[] initialValues) {
        return Optional.of(Arrays.asList(initialValues));
    }

    default CasePaymentOrder createCasePaymentOrder() {
        return CasePaymentOrder.builder()
            .caseId(Long.parseLong(CASE_ID_VALID_1))
            .action(ACTION)
            .responsibleParty(RESPONSIBLE_PARTY)
            .orderReference(ORDER_REFERENCE_VALID)
            .id(UUID.fromString(CPO_ID_VALID_1))
            .createdBy(CREATED_BY)
            .createdTimestamp(CREATED_TIMESTAMP)
            .historyExists(HISTORY_EXISTS_DEFAULT)
            .build();
    }

    default CasePaymentOrderEntity createCasePaymentOrderEntity() {
        CasePaymentOrderEntity entity = new CasePaymentOrderEntity();
        entity.setCaseId(Long.parseLong(CASE_ID_VALID_1));
        entity.setAction(ACTION);
        entity.setResponsibleParty(RESPONSIBLE_PARTY);
        entity.setOrderReference(ORDER_REFERENCE_VALID);
        entity.setCreatedBy(CREATED_BY);
        entity.setCreatedTimestamp(CREATED_TIMESTAMP);
        entity.setHistoryExists(HISTORY_EXISTS_DEFAULT);
        return entity;
    }

    default CreateCasePaymentOrderRequest createCreateCasePaymentOrderRequest() {
        return new CreateCasePaymentOrderRequest(
            CASE_ID_VALID_1,
            ACTION,
            RESPONSIBLE_PARTY,
            ORDER_REFERENCE_VALID
        );
    }

    default UpdateCasePaymentOrderRequest createUpdateCasePaymentOrderRequest() {
        return new UpdateCasePaymentOrderRequest(
            CPO_ID_VALID_1,
            CASE_ID_VALID_1,
            ACTION,
            RESPONSIBLE_PARTY,
            ORDER_REFERENCE_VALID
        );
    }

    default PageRequest getPageRequest() {
        return PageRequest.of(
            PAGE_NUMBER,
            PAGE_SIZE
        );
    }

    default Page<CasePaymentOrder> getDomainPages() {
        final PageRequest pageRequest = getPageRequest();
        return new PageImpl<>(createListOfCasePaymentOrder(), pageRequest, 3);
    }

    default List<CasePaymentOrder> createListOfCasePaymentOrder() {
        final ArrayList<CasePaymentOrder> casePaymentOrders = new ArrayList<>();

        final CasePaymentOrder casePaymentOrder = CasePaymentOrder.builder()
            .createdTimestamp(LocalDateTime.now())
            .caseId(1_234_123_412_341_234L)
            .action("Case Creation")
            .responsibleParty("The executor on the will")
            .orderReference("Bob123")
            .createdBy("Bob")
            .build();

        casePaymentOrders.add(casePaymentOrder);

        final CasePaymentOrder casePaymentOrder1 = CasePaymentOrder.builder()
            .createdTimestamp(LocalDateTime.now())
            .caseId(1_234_123_412_341_234L)
            .action("Case Creation")
            .responsibleParty("The executor on the will")
            .orderReference("Bob123")
            .createdBy("Bob")
            .build();

        casePaymentOrders.add(casePaymentOrder1);

        final CasePaymentOrder casePaymentOrder2 = CasePaymentOrder.builder()
            .createdTimestamp(LocalDateTime.now())
            .caseId(1_234_123_412_341_234L)
            .action("Case Creation")
            .responsibleParty("The executor on the will")
            .orderReference("Bob123")
            .createdBy("Bob")
            .build();

        casePaymentOrders.add(casePaymentOrder2);

        return casePaymentOrders;
    }

    default void assertGetCopPResponse(String expectedError, ResultActions response) throws Exception {
        response
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ERROR_PATH_STATUS).value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath(ERROR_PATH_ERROR).value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
            .andExpect(jsonPath(ERROR_PATH_MESSAGE, containsString(expectedError)));
    }

}
