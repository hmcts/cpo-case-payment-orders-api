package uk.gov.hmcts.reform.cpo.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;

@Mapper(componentModel = "spring")
public interface CasePaymentOrderMapper {

    CasePaymentOrderEntity toEntity(CasePaymentOrder casePaymentOrder);

    CasePaymentOrder toDomainModel(CasePaymentOrderEntity casePaymentOrderEntity);

    @Mapping(target = "createdTimestamp", expression = "java(java.time.LocalDateTime.now())")
    void mergeIntoEntity(@MappingTarget CasePaymentOrderEntity target,
                         UpdateCasePaymentOrderRequest updateCasePaymentOrderRequest,
                         String createdBy);
}
