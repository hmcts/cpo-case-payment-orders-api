package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersQueryBuilder;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    @Autowired
    private CasePaymentOrdersQueryBuilder casePaymentOrdersQueryBuilder;
    @Autowired
    private CasePaymentOrderMapper casePaymentOrderMapper;
    @Autowired
    private CasePaymentOrdersRepository casePaymentOrdersRepository;

    public CasePaymentOrdersServiceImpl(CasePaymentOrdersQueryBuilder casePaymentOrdersQueryBuilder, CasePaymentOrderMapper casePaymentOrderMapper, CasePaymentOrdersRepository casePaymentOrdersRepository) {
        this.casePaymentOrdersQueryBuilder = casePaymentOrdersQueryBuilder;
        this.casePaymentOrderMapper = casePaymentOrderMapper;
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
    }

    public List<CasePaymentOrder> getCasePaymentOrders(final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        final List<CasePaymentOrderEntity> result = casePaymentOrdersQueryBuilder.find(casePaymentOrderQueryFilter);
        return convertListToDomainModel(result);
    }

    private List<CasePaymentOrder> convertListToDomainModel(final List<CasePaymentOrderEntity> casePaymentOrderEntities) {
        return casePaymentOrderEntities.stream().map(
            casePaymentOrderEntity -> casePaymentOrderMapper.toDomainModel(casePaymentOrderEntity)
        ).collect(Collectors.toList());
    }

    //TODO THIS IS NOT GOING TO BE ADDED IN THE FINAL PR
    public void create() {

        final CasePaymentOrderEntity casePaymentOrderEntity = new CasePaymentOrderEntity();
        casePaymentOrderEntity.setAction("action");
        casePaymentOrderEntity.setCaseId(Long.parseLong("32"));
        casePaymentOrderEntity.setCreatedBy("action1");
        casePaymentOrderEntity.setOrderReference("action1");
        casePaymentOrderEntity.setEffectiveFrom(LocalDateTime.now());
        casePaymentOrderEntity.setCreatedTimestamp(LocalDateTime.now());
        casePaymentOrderEntity.setCaseTypeId("setCaseTypeId");
        casePaymentOrderEntity.setResponsibleParty("setResponsibleParty");
        casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity);

        final CasePaymentOrderEntity casePaymentOrderEntity1 = new CasePaymentOrderEntity();
        casePaymentOrderEntity1.setAction("action");
        casePaymentOrderEntity1.setCaseId(Long.parseLong("33"));
        casePaymentOrderEntity1.setCreatedBy("action1");
        casePaymentOrderEntity1.setOrderReference("action1");
        casePaymentOrderEntity1.setEffectiveFrom(LocalDateTime.now());
        casePaymentOrderEntity1.setCreatedTimestamp(LocalDateTime.now());
        casePaymentOrderEntity1.setCaseTypeId("setCaseTypeId");
        casePaymentOrderEntity1.setResponsibleParty("setResponsibleParty");
        casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity1);

        final CasePaymentOrderEntity casePaymentOrderEntity2 = new CasePaymentOrderEntity();
        casePaymentOrderEntity2.setAction("action");
        casePaymentOrderEntity2.setCaseId(Long.parseLong("34"));
        casePaymentOrderEntity2.setCreatedBy("action1");
        casePaymentOrderEntity2.setOrderReference("action1");
        casePaymentOrderEntity2.setEffectiveFrom(LocalDateTime.now());
        casePaymentOrderEntity2.setCreatedTimestamp(LocalDateTime.now());
        casePaymentOrderEntity2.setCaseTypeId("setCaseTypeId");
        casePaymentOrderEntity2.setResponsibleParty("setResponsibleParty");
        casePaymentOrdersRepository.saveAndFlush(casePaymentOrderEntity2);



    }
}
