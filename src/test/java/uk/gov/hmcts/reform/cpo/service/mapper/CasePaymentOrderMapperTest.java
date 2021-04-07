package uk.gov.hmcts.reform.cpo.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

class CasePaymentOrderMapperTest {

    private CasePaymentOrderMapperImpl mapper;

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
    void successfulMap() {
        CasePaymentOrder mappedDomainObject = mapper.toDomainModel(entity);
        assertEquals("Mapped domain model created timestamp should equals mocked domain model created timestamp",
                     casePaymentOrder.getCreatedTimestamp(), mappedDomainObject.getCreatedTimestamp());
        assertEquals("Mapped domain model effective from should equals mocked domain model effective from",
                     casePaymentOrder.getEffectiveFrom(), mappedDomainObject.getEffectiveFrom());
        assertEquals("Mapped domain model case id should equals mocked domain model case id",
                     casePaymentOrder.getCaseId(), mappedDomainObject.getCaseId());
        assertEquals("Mapped domain model action should equals mocked domain model action",
                     casePaymentOrder.getAction(), mappedDomainObject.getAction());
        assertEquals("Mapped domain model responsible party should equals mocked domain model responsible party",
                     casePaymentOrder.getResponsibleParty(), mappedDomainObject.getResponsibleParty());
        assertEquals("Mapped domain model order reference should equals mocked domain model order reference",
                     casePaymentOrder.getOrderReference(), mappedDomainObject.getOrderReference());
        assertEquals("Mapped domain model created by should equals mocked domain model created by",
                     casePaymentOrder.getCreatedBy(), mappedDomainObject.getCreatedBy());
    }

    @Test
    void successfulDomainMapping() {

        final List<CasePaymentOrderEntity> casePaymentOrderEntities = new ArrayList<>();
        casePaymentOrderEntities.add(entity);
        final List<CasePaymentOrder> casePaymentOrders = mapper.map(casePaymentOrderEntities);

        assertTrue("The expected size is 1", casePaymentOrders.size() == 1);
        CasePaymentOrder mappedDomainObject = casePaymentOrders.get(0);
        assertEquals("Mapped domain model created timestamp should equals mocked domain model created timestamp",
                     casePaymentOrder.getCreatedTimestamp(), mappedDomainObject.getCreatedTimestamp());
        assertEquals("Mapped domain model effective from should equals mocked domain model effective from",
                     casePaymentOrder.getEffectiveFrom(), mappedDomainObject.getEffectiveFrom());
        assertEquals("Mapped domain model case id should equals mocked domain model case id",
                     casePaymentOrder.getCaseId(), mappedDomainObject.getCaseId());
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
