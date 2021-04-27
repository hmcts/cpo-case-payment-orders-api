package uk.gov.hmcts.reform.cpo.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

class CasePaymentOrderMapperTest implements BaseTest {

    @InjectMocks
    private CasePaymentOrderMapperImpl mapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void successfulEntityMapping() {

        // GIVEN
        CasePaymentOrder casePaymentOrder = createCasePaymentOrder();

        // WHEN
        CasePaymentOrderEntity mappedEntity = mapper.toEntity(casePaymentOrder);

        // THEN
        assertEquals("Mapped entity created timestamp should equals mocked entity created timestamp",
                     casePaymentOrder.getCreatedTimestamp().truncatedTo(ChronoUnit.SECONDS),
                     mappedEntity.getCreatedTimestamp().truncatedTo(ChronoUnit.SECONDS));
        assertEquals("Mapped entity effective from should equals mocked entity effective from",
                     casePaymentOrder.getEffectiveFrom(), mappedEntity.getEffectiveFrom());
        assertEquals("Mapped entity case id should equals mocked entity case id",
                     casePaymentOrder.getCaseId(), mappedEntity.getCaseId());
        assertEquals("Mapped entity action should equals mocked entity action",
                     casePaymentOrder.getAction(), mappedEntity.getAction());
        assertEquals("Mapped entity responsible party should equals mocked entity responsible party",
                     casePaymentOrder.getResponsibleParty(), mappedEntity.getResponsibleParty());
        assertEquals("Mapped entity order reference should equals mocked entity order reference",
                     casePaymentOrder.getOrderReference(), mappedEntity.getOrderReference());
        assertEquals("Mapped entity created by should equals mocked entity created by",
                     casePaymentOrder.getCreatedBy(), mappedEntity.getCreatedBy());
    }

    @Test
    void successfulDomainMapping() {

        // GIVEN
        CasePaymentOrderEntity entity = createCasePaymentOrderEntity();

        // WHEN
        CasePaymentOrder mappedDomainObject = mapper.toDomainModel(entity);

        // THEN
        assertEquals("Mapped domain model created timestamp should equals mocked domain model"
                         + " created timestamp",
                     entity.getCreatedTimestamp(), mappedDomainObject.getCreatedTimestamp());
        assertEquals("Mapped domain model effective from should equals mocked domain model effective from",
                     entity.getEffectiveFrom(), mappedDomainObject.getEffectiveFrom());
        assertEquals("Mapped domain model case id should equals mocked domain model case id",
                     entity.getCaseId(), mappedDomainObject.getCaseId());
        assertEquals("Mapped domain model action should equals mocked domain model action",
                     entity.getAction(), mappedDomainObject.getAction());
        assertEquals("Mapped domain model responsible party should equals mocked domain model "
                         + "responsible party",
                     entity.getResponsibleParty(), mappedDomainObject.getResponsibleParty());
        assertEquals("Mapped domain model order reference should equals mocked domain model order reference",
                     entity.getOrderReference(), mappedDomainObject.getOrderReference());
        assertEquals("Mapped domain model created by should equals mocked domain model created by",
                     entity.getCreatedBy(), mappedDomainObject.getCreatedBy());
    }

    @DisplayName("should successfully map an create request into an entity")
    @Test
    void successfulCreateRequestToEntityMapping() {

        // GIVEN
        CreateCasePaymentOrderRequest request = createCreateCasePaymentOrderRequest();

        // WHEN
        CasePaymentOrderEntity mappedRequestEntity = mapper.toEntity(request, CREATED_BY);

        // THEN
        assertEquals("Mapped entity effective from should equals mocked entity effective from",
                     request.getEffectiveFrom(), mappedRequestEntity.getEffectiveFrom());
        assertEquals("Mapped entity case id should equals mocked entity case id",
                     request.getCaseId(), mappedRequestEntity.getCaseId().toString());
        assertEquals("Mapped entity action should equals mocked entity action",
                     request.getAction(), mappedRequestEntity.getAction());
        assertEquals("Mapped entity responsible party should equals mocked entity responsible party",
                     request.getResponsibleParty(), mappedRequestEntity.getResponsibleParty());
        assertEquals("Mapped entity order reference should equals mocked entity order reference",
                     request.getOrderReference(), mappedRequestEntity.getOrderReference());
        assertEquals("Mapped entity created by should equals mocked entity created by",
                     CREATED_BY, mappedRequestEntity.getCreatedBy());
    }

    @DisplayName("should successfully merge an update request into an entity")
    @Test
    void successfulMergeUpdateRequestIntoEntityMapping() {

        // GIVEN
        CasePaymentOrderEntity entity = createCasePaymentOrderEntity();
        // create UpdateRequest using DIFFERENT data to that in BaseTest
        UpdateCasePaymentOrderRequest updateRequest =
            new UpdateCasePaymentOrderRequest(UUID.randomUUID().toString(),
                                              LocalDateTime.of(2020, 10, 1, 12, 30, 0, 0),
                                              CASE_ID_VALID_2,
                                              "Merge Action",
                                              "Merge Responsible Party",
                                              "Merge Order Reference");
        final LocalDateTime originalCreatedTimestamp = entity.getCreatedTimestamp();
        final String sourceCreatedBy = "Merge Created By";

        // WHEN
        mapper.mergeIntoEntity(entity, updateRequest, sourceCreatedBy);

        // THEN
        // check created/modifided data
        assertEquals("Merged created by should equals supplied created by",
                     sourceCreatedBy, entity.getCreatedBy());
        assertNotNull("Merged created timestamp should be set", entity.getCreatedTimestamp());
        assertNotEquals("Merged created timestamp should have changed",
                        originalCreatedTimestamp, entity.getCreatedTimestamp());
        // standard property checks
        assertEquals("Merged effective from should equal source effective from",
                     updateRequest.getEffectiveFrom(), entity.getEffectiveFrom());
        assertEquals("Merged case id should equal source case id",
                     updateRequest.getCaseId(), entity.getCaseId().toString());
        assertEquals("Merged action should equal source action",
                     updateRequest.getAction(), entity.getAction());
        assertEquals("Merged responsible party should equal source responsible party",
                     updateRequest.getResponsibleParty(), entity.getResponsibleParty());
        assertEquals("Merged order reference should equal source order reference",
                     updateRequest.getOrderReference(), entity.getOrderReference());

    }

    @DisplayName("should successfully run mergeIntoEntity when all inputs are null and leave entity unchanged")
    @Test
    void successfulMergeUpdateRequestIntoEntityMappingUsingAllNullLeavesEntityUnchanged() {

        // GIVEN
        CasePaymentOrderEntity entity = createCasePaymentOrderEntity();
        final LocalDateTime originalCreatedTimestamp = entity.getCreatedTimestamp();
        final String originalCreatedBy = entity.getCreatedBy();

        // WHEN
        mapper.mergeIntoEntity(entity, null, null);

        // THEN
        // check created/modified data (UNCHANGED)
        assertEquals("Created by should be unchanged",
                     originalCreatedBy, entity.getCreatedBy());
        assertEquals("Created timestamp should be unchanged",
                     originalCreatedTimestamp, entity.getCreatedTimestamp());
        // standard property checks (not null: assume unchanged)
        assertNotNull("Effective from should remain populated", entity.getEffectiveFrom());
        assertNotNull("Case ID should remain populated", entity.getCaseId());
        assertNotNull("Action should remain populated", entity.getAction());
        assertNotNull("Responsible party should remain populated", entity.getResponsibleParty());
        assertNotNull("Order reference should remain populated", entity.getOrderReference());

    }

}
