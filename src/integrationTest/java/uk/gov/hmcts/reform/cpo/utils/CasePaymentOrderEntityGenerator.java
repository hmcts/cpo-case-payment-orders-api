package uk.gov.hmcts.reform.cpo.utils;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersJpaRepository;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CasePaymentOrderEntityGenerator {

    @Autowired
    private CasePaymentOrdersJpaRepository casePaymentOrdersJpaRepository;

    @Autowired
    private CasePaymentOrderMapper casePaymentOrderMapper;

    @Autowired
    private UIDService uidService;

    private List<CasePaymentOrderEntity> generateAndSaveEntities(int numberToGenerate, boolean useSameCaseId) {

        Long caseId = Long.parseLong(uidService.generateUID());

        List<CasePaymentOrderEntity> returnValue = new ArrayList<>();

        for (int i = 0; i < numberToGenerate; i++) {
            CasePaymentOrder casePaymentOrder = CasePaymentOrder.builder()
                    .action("Action " + RandomUtils.nextInt())
                    .caseId(useSameCaseId ? caseId : Long.parseLong(uidService.generateUID()))
                    .createdBy("Created by " + RandomUtils.nextBytes(2))
                    .orderReference("order ref " + RandomUtils.nextBytes(3))
                    .effectiveFrom(LocalDateTime.now())
                    .createdTimestamp(LocalDateTime.now())
                    .responsibleParty("ResponsibleParty" + RandomUtils.nextBytes(2))
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

}
