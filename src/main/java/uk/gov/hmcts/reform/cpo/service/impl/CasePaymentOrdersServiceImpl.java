package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    private CasePaymentOrdersRepository casePaymentOrdersRepository;
    private CasePaymentOrderMapper casePaymentOrderMapper;
    public static final Integer DEFAULT_PAGE_SIZE = 20;
    public static final Integer DEFAULT_PAGE_NUMBER = 1;

    public List<CasePaymentOrder> getCasePaymentOrders(final Optional<List<String>> ids, final Optional<List<String>> casesId,
                                                       final Integer pageSize, final Integer pageNumber) {

        final List<String> listOfIds = ids.orElse(Collections.emptyList());
        final List<String> listOfCasesIds = casesId.orElse(Collections.emptyList());
        if (listOfIds.isEmpty() && listOfCasesIds.isEmpty() || !listOfIds.isEmpty() && !listOfCasesIds.isEmpty()) {
            //TODO ERRORS CODES AND throw a proper exception with a generic error message.
            throw new RuntimeException("AN ERROR TO BE DEFINED");
        }
        final PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        if(!listOfIds.isEmpty()){
            return convertListToDomainModel(casePaymentOrdersRepository.getCasePaymentOrdersByIds(
                listOfIds,
                pageRequest
            ).getContent());

        }
        if(!listOfCasesIds.isEmpty()){
            return convertListToDomainModel(casePaymentOrdersRepository.getCasePaymentOrdersByCaseIds(
                listOfCasesIds,
                pageRequest
            ).getContent());
        }
        //TODO ERRORS CODES AND throw a proper exception with a generic error message.
        throw new RuntimeException("AN ERROR TO BE DEFINED");
    }

    private  List<CasePaymentOrder> convertListToDomainModel(final List<CasePaymentOrderEntity> casePaymentOrderEntities){
        return casePaymentOrderEntities.stream().map(
            casePaymentOrderEntity-> casePaymentOrderMapper.toDomainModel(casePaymentOrderEntity)
        ).collect(Collectors.toList());
    }
}
