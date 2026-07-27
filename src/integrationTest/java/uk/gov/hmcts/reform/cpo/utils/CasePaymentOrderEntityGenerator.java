package uk.gov.hmcts.reform.cpo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersJpaRepository;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CasePaymentOrderEntityGenerator {

    @Autowired
    private CasePaymentOrdersJpaRepository casePaymentOrdersJpaRepository;

    @Autowired
    private CasePaymentOrderMapper casePaymentOrderMapper;

    @Autowired
    private UIDService uidService;

    private List<CasePaymentOrderEntity> generateAndSaveEntities(int numberToGenerate, boolean useSameCaseId) {

        Long caseId = Long.parseLong(generateUniqueCaseId());

        List<CasePaymentOrderEntity> returnValue = new ArrayList<>();

        for (int i = 0; i < numberToGenerate; i++) {
            CasePaymentOrder casePaymentOrder = CasePaymentOrder.builder()
                .action("Action " + ThreadLocalRandom.current().nextInt())
                .caseId(useSameCaseId ? caseId : Long.parseLong(generateUniqueCaseId()))
                .createdBy("Created by " + UUID.randomUUID())
                .orderReference("2021-" + ThreadLocalRandom.current()
                    .nextLong(1000000000000L, 9999999999999L))
                .createdTimestamp(LocalDateTime.now())
                .responsibleParty("ResponsibleParty" + UUID.randomUUID())
                .historyExists(false)
                .build();


            CasePaymentOrderEntity savedEntity =
                casePaymentOrdersJpaRepository.saveAndFlush(casePaymentOrderMapper.toEntity(casePaymentOrder));
            returnValue.add(savedEntity);
        }

        return returnValue;
    }

    public List<CasePaymentOrderEntity> generateAndSaveEntities(int numberToGenerate) {
        return generateAndSaveEntities(numberToGenerate, false);
    }

    public List<CasePaymentOrderEntity> generateAndSaveEntitiesWithSameCaseId(int numberToGenerate) {
        return generateAndSaveEntities(numberToGenerate, true);
    }

    public String generateUniqueCaseId() {
        return uidService.generateUID();
    }

}
