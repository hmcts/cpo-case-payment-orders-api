package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.SecurityUtils;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import javax.transaction.Transactional;


@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    private final SecurityUtils securityUtils;

    private final CasePaymentOrderMapper mapper;

    private final CasePaymentOrdersRepository casePaymentOrdersRepository;

    public CasePaymentOrdersServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository,
                                        CasePaymentOrderMapper mapper, SecurityUtils securityUtils) {
        this.mapper = mapper;
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    @Override
    public CasePaymentOrder createCasePaymentOrder(CreateCasePaymentOrderRequest createCasePaymentOrderRequest) {
        String createdBy = securityUtils.getUserInfo().getUid();
        CasePaymentOrderEntity requestEntity = mapper.toEntity(createCasePaymentOrderRequest, createdBy);
        CasePaymentOrderEntity savedEntity = casePaymentOrdersRepository.save(requestEntity);
        return mapper.toDomainModel(savedEntity);
    }
}
