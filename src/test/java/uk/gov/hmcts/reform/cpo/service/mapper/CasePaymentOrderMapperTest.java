package uk.gov.hmcts.reform.cpo.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertEquals;

public class CasePaymentOrderMapperTest {

    private CasePaymentOrderMapperImpl mapper;

    private CasePaymentOrder casePaymentOrder;
    private CasePaymentOrderEntity entity;

    @BeforeEach
    public void setUp() {
        mapper = new CasePaymentOrderMapperImpl();

        UUID id = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();

        casePaymentOrder = CasePaymentOrder.builder()
            .id(id)
            .createdTimestamp(date)
            .effectiveFrom(date)
            .caseId("1234123412341234")
            .caseTypeId("Probate")
            .action("Case Creation")
            .responsibleParty("The executor on the will")
            .orderReference("Bob123")
            .createdBy("Bob")
            .build();

        entity = new CasePaymentOrderEntity();
        entity.setId(id);
        entity.setCreatedTimestamp(date);
        entity.setEffectiveFrom(date);
        entity.setCaseId("1234123412341234");
        entity.setCaseTypeId("Probate");
        entity.setAction("Case Creation");
        entity.setResponsibleParty("The executor on the will");
        entity.setOrderReference("Bob123");
        entity.setCreatedBy("Bob");
    }

    @Test
    public void successfulEntityMapping() {
        CasePaymentOrderEntity mappedEntity = mapper.toEntity(casePaymentOrder);
        assertEquals("Mapped entity id should equals mocked entity id",
                     entity.getId(), mappedEntity.getId());
        assertEquals("Mapped entity created timestamp should equals mocked entity created timestamp",
                     entity.getCreatedTimestamp(), mappedEntity.getCreatedTimestamp());
        assertEquals("Mapped entity effective from should equals mocked entity effective from",
                     entity.getEffectiveFrom(), mappedEntity.getEffectiveFrom());
        assertEquals("Mapped entity case id should equals mocked entity case id",
                     entity.getCaseId(), mappedEntity.getCaseId());
        assertEquals("Mapped entity case type id should equals mocked entity case type id",
                     entity.getCaseTypeId(), mappedEntity.getCaseTypeId());
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
    public void successfulDomainMapping() {
        CasePaymentOrder mappedDomainObject = mapper.toDomainModel(entity);
        assertEquals("Mapped domain model id should equals mocked domain model id",
                     casePaymentOrder.getId(), mappedDomainObject.getId());
        assertEquals("Mapped domain model created timestamp should equals mocked domain model created timestamp",
                     casePaymentOrder.getCreatedTimestamp(), mappedDomainObject.getCreatedTimestamp());
        assertEquals("Mapped domain model effective from should equals mocked domain model effective from",
                     casePaymentOrder.getEffectiveFrom(), mappedDomainObject.getEffectiveFrom());
        assertEquals("Mapped domain model case id should equals mocked domain model case id",
                     casePaymentOrder.getCaseId(), mappedDomainObject.getCaseId());
        assertEquals("Mapped domain model case type id should equals mocked domain model case type id",
                     casePaymentOrder.getCaseTypeId(), mappedDomainObject.getCaseTypeId());
        assertEquals("Mapped domain model action should equals mocked domain model action",
                     casePaymentOrder.getAction(), mappedDomainObject.getAction());
        assertEquals("Mapped domain model responsible party should equals mocked domain model responsible party",
                     casePaymentOrder.getResponsibleParty(), mappedDomainObject.getResponsibleParty());
        assertEquals("Mapped domain model order reference should equals mocked domain model order reference",
                     casePaymentOrder.getOrderReference(), mappedDomainObject.getOrderReference());
        assertEquals("Mapped domain model created by should equals mocked domain model created by",
                     casePaymentOrder.getCreatedBy(), mappedDomainObject.getCreatedBy());
    }
}
