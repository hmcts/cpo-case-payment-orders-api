package uk.gov.hmcts.reform;

import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaseTest {

    public static final String ERROR_PATH_DETAILS = "$.details";
    public static final String ERROR_PATH_ERROR = "$.error";
    public static final String ERROR_PATH_MESSAGE = "$.message";
    public static final String ERROR_PATH_STATUS = "$.status";

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

    public static final String ORDER_REFERENCE_VALID = "2021-11223344556";
    public static final String ORDER_REFERENCE_INVALID = "2021-918425346";
    String ACTION = "action";
    String RESPONSIBLE_PARTY = "responsibleParty";
    LocalDateTime EFFECTIVE_FROM = LocalDateTime.of(2021, Month.MARCH, 24, 11, 48, 32);

    String CREATED_BY = "createdBy";
    LocalDateTime CREATED_TIMESTAMP = LocalDateTime.now();

    default <T> Optional<List<T>> createInitialValuesList(final T[] initialValues) {
        return Optional.of(Arrays.asList(initialValues));
    }

    default CasePaymentOrder createCasePaymentOrder() {
        return CasePaymentOrder.builder()
            .caseId(Long.parseLong(CASE_ID_VALID_1))
            .effectiveFrom(EFFECTIVE_FROM)
            .action(ACTION)
            .responsibleParty(RESPONSIBLE_PARTY)
            .orderReference(ORDER_REFERENCE_VALID)
            .id(UUID.fromString(CPO_ID_VALID_1))
            .createdBy(CREATED_BY)
            .createdTimestamp(CREATED_TIMESTAMP)
            .build();
    }

    default CasePaymentOrderEntity createCasePaymentOrderEntity() {
        CasePaymentOrderEntity entity = new CasePaymentOrderEntity();
        entity.setEffectiveFrom(EFFECTIVE_FROM);
        entity.setCaseId(Long.parseLong(CASE_ID_VALID_1));
        entity.setAction(ACTION);
        entity.setResponsibleParty(RESPONSIBLE_PARTY);
        entity.setOrderReference(ORDER_REFERENCE_VALID);
        entity.setCreatedBy(CREATED_BY);
        entity.setCreatedTimestamp(CREATED_TIMESTAMP);
        return entity;
    }

    default CreateCasePaymentOrderRequest createCreateCasePaymentOrderRequest() {
        return new CreateCasePaymentOrderRequest(
            EFFECTIVE_FROM,
            CASE_ID_VALID_1,
            ACTION,
            RESPONSIBLE_PARTY,
            ORDER_REFERENCE_VALID
        );
    }

    default UpdateCasePaymentOrderRequest createUpdateCasePaymentOrderRequest() {
        return new UpdateCasePaymentOrderRequest(
            CPO_ID_VALID_1,
            LocalDateTime.now(),
            CASE_ID_VALID_1,
            ACTION,
            RESPONSIBLE_PARTY,
            ORDER_REFERENCE_VALID
        );
    }

}
