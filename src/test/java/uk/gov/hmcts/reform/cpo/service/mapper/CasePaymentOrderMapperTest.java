package uk.gov.hmcts.reform.cpo.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    private CasePaymentOrderMapperImpl mapper;
    private CreateCasePaymentOrderRequest request;
    private CasePaymentOrder casePaymentOrder;
    private CasePaymentOrderEntity entity;

    @BeforeEach
    public void setUp() {
        mapper = new CasePaymentOrderMapperImpl();

        LocalDateTime date = LocalDateTime.now();

        entity = new CasePaymentOrderEntity();
        entity.setCreatedTimestamp(date);
        entity.setEffectiveFrom(date);
        entity.setCaseId(1_234_123_412_341_234L);
        entity.setAction("Case Creation");
        entity.setResponsibleParty("The executor on the will");
        entity.setOrderReference("Bob123");
        entity.setCreatedBy("Bob");

        casePaymentOrder = CasePaymentOrder.builder()
            .createdTimestamp(entity.getCreatedTimestamp())
            .effectiveFrom(date)
            .caseId(1_234_123_412_341_234L)
            .action("Case Creation")
            .responsibleParty("The executor on the will")
            .orderReference("Bob123")
            .createdBy("Bob")
            .build();

        request = new CreateCasePaymentOrderRequest(date, "1122334455667788",
                                                    "Case Submit", "Jane Doe",
                                                    "2021-918425346");
        // create entity and domain model from same sample data in `BaseTest`
        entity = createCasePaymentOrderEntity();
        casePaymentOrder = createCasePaymentOrder();
    }

    @Test
    void successfulEntityMapping() {
        CasePaymentOrderEntity mappedEntity = mapper.toEntity(casePaymentOrder);
        assertEquals("Mapped entity created timestamp should equals mocked entity created timestamp",
                     entity.getCreatedTimestamp().truncatedTo(ChronoUnit.SECONDS),
                     mappedEntity.getCreatedTimestamp().truncatedTo(ChronoUnit.SECONDS));
        assertEquals("Mapped entity effective from should equals mocked entity effective from",
                     entity.getEffectiveFrom(), mappedEntity.getEffectiveFrom());
        assertEquals("Mapped entity case id should equals mocked entity case id",
                     entity.getCaseId(), mappedEntity.getCaseId());
        assertEquals("Mapped entity action should equals mocked entity action",
                     entity.getAction(), mappedEntity.getAction());
        assertEquals("Mapped entity responsible party should equals mocked entity responsible party",
                     entity.getResponsibleParty(), mappedEntity.getResponsibleParty());
        assertEquals("Mapped entity order reference should equals mocked entity order reference",
                     entity.getOrderReference(), mappedEntity.getOrderReference());
        assertEquals("Mapped entity created by should equals mocked entity created by",
                     entity.getCreatedBy(), mappedEntity.getCreatedBy());
    }

    @Test
    void successfulDomainMapping() {
        CasePaymentOrder mappedDomainObject = mapper.toDomainModel(entity);
        assertEquals("Mapped domain model created timestamp should equals mocked domain model"
                         + " created timestamp",
                     casePaymentOrder.getCreatedTimestamp(), mappedDomainObject.getCreatedTimestamp());
        assertEquals("Mapped domain model effective from should equals mocked domain model effective from",
                     casePaymentOrder.getEffectiveFrom(), mappedDomainObject.getEffectiveFrom());
        assertEquals("Mapped domain model case id should equals mocked domain model case id",
                     casePaymentOrder.getCaseId(), mappedDomainObject.getCaseId());
        assertEquals("Mapped domain model action should equals mocked domain model action",
                     casePaymentOrder.getAction(), mappedDomainObject.getAction());
        assertEquals("Mapped domain model responsible party should equals mocked domain model "
                         + "responsible party",
                     casePaymentOrder.getResponsibleParty(), mappedDomainObject.getResponsibleParty());
        assertEquals("Mapped domain model order reference should equals mocked domain model order reference",
                     casePaymentOrder.getOrderReference(), mappedDomainObject.getOrderReference());
        assertEquals("Mapped domain model created by should equals mocked domain model created by",
                     casePaymentOrder.getCreatedBy(), mappedDomainObject.getCreatedBy());
    }

    @DisplayName("should successfully merge an update request into an entity")
    @Test
    void successfulMergeIntoEntityMapping() {

        // GIVEN
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
    void successfulMergeIntoEntityMappingUsingAllNullLeavesEntityUnchanged() {

        // GIVEN
        final LocalDateTime originalCreatedTimestamp = entity.getCreatedTimestamp();
        final String originalCreatedBy = entity.getCreatedBy();

        // WHEN
        mapper.mergeIntoEntity(entity, null, null);

        // THEN
        // check created/modifided data (UNCHANGED)
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


    @Test
    void successfulRequestToEntityMapping() {
        CasePaymentOrderEntity mappedRequestEntity = mapper.toEntity(request, "Jane");
        assertEquals("Mapped entity effective from should equals mocked entity effective from",
                     request.getEffectiveFrom(), mappedRequestEntity.getEffectiveFrom());
        assertEquals("Mapped entity case id should equals mocked entity case id",
                     1_122_334_455_667_788L, mappedRequestEntity.getCaseId());
        assertEquals("Mapped entity action should equals mocked entity action",
                     request.getAction(), mappedRequestEntity.getAction());
        assertEquals("Mapped entity responsible party should equals mocked entity responsible party",
                     request.getResponsibleParty(), mappedRequestEntity.getResponsibleParty());
        assertEquals("Mapped entity order reference should equals mocked entity order reference",
                     request.getOrderReference(), mappedRequestEntity.getOrderReference());
        assertEquals("Mapped entity created by should equals mocked entity created by",
                     "Jane", mappedRequestEntity.getCreatedBy());
    }
}
