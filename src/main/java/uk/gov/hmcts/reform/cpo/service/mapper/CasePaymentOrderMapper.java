package uk.gov.hmcts.reform.cpo.service.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CasePaymentOrderMapper {

    CasePaymentOrderEntity toEntity(CasePaymentOrder casePaymentOrder);

    CasePaymentOrder toDomainModel(CasePaymentOrderEntity casePaymentOrderEntity);

    List<CasePaymentOrder> map(List<CasePaymentOrderEntity> casePaymentOrderEntities);
}

