package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {


    private final CasePaymentOrderMapper mapper;

    private final CasePaymentOrdersRepository casePaymentOrdersRepository;

    public CasePaymentOrdersServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository,
                                        CasePaymentOrderMapper mapper) {
        this.mapper = mapper;
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
    }

    @Override
    public UUID createCasePaymentOrder(CasePaymentOrderRequest request) {
        if (checkParametersPresent(request)) {
            String createdBy = request.getUserToken();//.getIdamId() somehow
            CasePaymentOrder casePaymentOrder = mapper.toRequest(request, createdBy, LocalDateTime.now());
            CasePaymentOrderEntity requestEntity = mapper.toEntity(casePaymentOrder);
            CasePaymentOrderEntity returnEntity = casePaymentOrdersRepository.saveAndFlush(requestEntity);
            return returnEntity.getId();
            }
        return null;
    }

    @Override
    public boolean checkParametersPresent(CasePaymentOrderRequest request) {
        return request.getEffectiveFrom() != null && request.getCaseId() != null
            && request.getCaseTypeId() != null && request.getAction() != null
            && request.getResponsibleParty() != null && request.getOrderReference() != null
            && request.getUserToken() != null;
    }
}
