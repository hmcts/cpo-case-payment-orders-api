package uk.gov.hmcts.reform.cpo.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class CasePaymentOrderMapperTest {

    @Autowired
    private CasePaymentOrderMapper mapper;

    private CasePaymentOrder casePaymentOrder;
    private CasePaymentOrderEntity entity;

    @BeforeEach
    public void setUp() {
        Date date = Date.from(Instant.now());

        casePaymentOrder = new CasePaymentOrder();
        casePaymentOrder.setId(1L);
        casePaymentOrder.setCreatedTimestamp(date);
        casePaymentOrder.setEffectiveFrom(date);
        casePaymentOrder.setCaseId("1234123412341234");
        casePaymentOrder.setCaseTypeId("Probate");
        casePaymentOrder.setAction("Case Creation");
        casePaymentOrder.setResponsibleParty("The executor on the will");
        casePaymentOrder.setOrderReference("Bob123");
        casePaymentOrder.setCreatedBy("Bob");

        entity = new CasePaymentOrderEntity();
        entity.setId(1L);
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
        assertEquals("Mapped entity should equals mocked entity",
                     entity, mappedEntity);
    }

    @Test
    public void successfulDomainMapping() {
        CasePaymentOrder mappedDomainObject = mapper.toDomainModel(entity);
        assertEquals("Mapped domain model should equals mocked domain model",
                     casePaymentOrder, mappedDomainObject);
    }
}
