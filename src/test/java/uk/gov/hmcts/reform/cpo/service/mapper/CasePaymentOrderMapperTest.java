package uk.gov.hmcts.reform.cpo.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class CasePaymentOrderMapperTest {

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
