package uk.gov.hmcts.reform.cpo.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;

@Mapper(componentModel = "spring")
public interface CasePaymentOrderMapper {

    CasePaymentOrderEntity toEntity(CasePaymentOrder casePaymentOrder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdTimestamp", ignore = true)
    CasePaymentOrderEntity toEntity(CreateCasePaymentOrderRequest createCasePaymentOrderRequest,
                                                String createdBy);

    CasePaymentOrder toDomainModel(CasePaymentOrderEntity casePaymentOrderEntity);
}
