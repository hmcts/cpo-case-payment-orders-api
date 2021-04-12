package uk.gov.hmcts.reform;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public interface BaseTest {

    String IDS = "ids";
    String CASE_IDS = "case-ids";
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

    String ORDER_REFERENCE = "orderReference";
    String ACTION = "action";
    String RESPONSIBLE_PARTY = "responsibleParty";
    LocalDateTime EFFECTIVE_FROM = LocalDateTime.of(2021, Month.MARCH, 24, 11, 48, 32);

    String CREATED_BY = "createdBy";
    LocalDateTime CREATED_TIMESTAMP = LocalDateTime.now();

    String UN_EXPECTED_ERROR_IN_TEST = "Fail due an expected error in the test.";

    default <T> Optional<List<T>> createInitialValuesList(final T[] initialValues) {
        return Optional.of(Arrays.asList(initialValues));
    }


    default CasePaymentOrder createCasePaymentOrder() {
        return CasePaymentOrder.builder()
            .caseId(Long.parseLong(CASE_ID_VALID_1))
            .effectiveFrom(EFFECTIVE_FROM)
            .action(ACTION)
            .responsibleParty(RESPONSIBLE_PARTY)
            .orderReference(ORDER_REFERENCE)
            .id(UUID.fromString(CPO_ID_VALID_1))
            .createdBy(CREATED_BY)
            .createdTimestamp(CREATED_TIMESTAMP)
            .build();
    }

    default UpdateCasePaymentOrderRequest createUpdateCasePaymentOrderRequest() {
        return new UpdateCasePaymentOrderRequest(
            CPO_ID_VALID_1,
            LocalDateTime.now(),
            CASE_ID_VALID_1,
            ORDER_REFERENCE,
            ACTION,
            RESPONSIBLE_PARTY
        );
    }

    default CasePaymentOrderQueryFilter getACasePaymentOrderQueryFilter(int  pageSize, List<String> casesIds,
                                                                        List<String> ids) {

        return CasePaymentOrderQueryFilter.builder()
            .cpoIds(ids)
            .caseIds(casesIds)
            .pageNumber(CasePaymentOrderQueryFilter.PAGE_NUMBER)
            .pageSize(pageSize)
            .build();
    }

    default PageRequest getPageRequest(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        return PageRequest.of(
            casePaymentOrderQueryFilter.getPageNumber(),
            casePaymentOrderQueryFilter.getPageSize()
        );
    }

    default Page<CasePaymentOrder> getDomainPages(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        final PageRequest pageRequest = getPageRequest(casePaymentOrderQueryFilter);
        return new PageImpl<CasePaymentOrder>(createListOfCasePaymentOrder(), pageRequest, 3);
    }

    default List<CasePaymentOrder> createListOfCasePaymentOrder() {
        final ArrayList<CasePaymentOrder> casePaymentOrders = new ArrayList<>();

        final CasePaymentOrder casePaymentOrder = CasePaymentOrder.builder()
            .createdTimestamp(LocalDateTime.now())
            .effectiveFrom(LocalDateTime.now())
            .caseId(1_234_123_412_341_234L)
            .action("Case Creation")
            .responsibleParty("The executor on the will")
            .orderReference("Bob123")
            .createdBy("Bob")
            .build();

        casePaymentOrders.add(casePaymentOrder);

        final CasePaymentOrder casePaymentOrder1 = CasePaymentOrder.builder()
            .createdTimestamp(LocalDateTime.now())
            .effectiveFrom(LocalDateTime.now())
            .caseId(1_234_123_412_341_234L)
            .action("Case Creation")
            .responsibleParty("The executor on the will")
            .orderReference("Bob123")
            .createdBy("Bob")
            .build();

        casePaymentOrders.add(casePaymentOrder1);

        final CasePaymentOrder casePaymentOrder2 = CasePaymentOrder.builder()
            .createdTimestamp(LocalDateTime.now())
            .effectiveFrom(LocalDateTime.now())
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
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
            .andExpect(jsonPath("$.message",containsString(expectedError)));
    }
}
