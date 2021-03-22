package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.security.IdamRepository;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import javax.transaction.Transactional;


@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {
    private final IdamRepository idamRepository;

    private final CasePaymentOrderMapper mapper;

    private final CasePaymentOrdersRepository casePaymentOrdersRepository;

    public CasePaymentOrdersServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository,
                                        CasePaymentOrderMapper mapper, IdamRepository idamRepository) {
        this.mapper = mapper;
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
        this.idamRepository = idamRepository;
    }

    @Transactional
    @Override
    public CasePaymentOrder createCasePaymentOrder(CreateCasePaymentOrderRequest createCasePaymentOrderRequest,
                                                   String userToken) {
        String createdBy = idamRepository.getUserInfo(userToken).getUid();
        CasePaymentOrder casePaymentOrder = mapper.fromCreateCasePaymentOrder(createCasePaymentOrderRequest, createdBy);
        CasePaymentOrderEntity requestEntity = mapper.toEntity(casePaymentOrder);
        CasePaymentOrderEntity returnEntity = casePaymentOrdersRepository.save(requestEntity);
        return mapper.toDomainModel(returnEntity);
    }
}
