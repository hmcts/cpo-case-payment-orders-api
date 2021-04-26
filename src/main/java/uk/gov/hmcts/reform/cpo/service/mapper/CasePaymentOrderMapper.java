package uk.gov.hmcts.reform.cpo.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CasePaymentOrderMapper {

    CasePaymentOrderEntity toEntity(CasePaymentOrder casePaymentOrder);

    CasePaymentOrderEntity toEntity(CreateCasePaymentOrderRequest createCasePaymentOrderRequest,
                                                String createdBy);

    CasePaymentOrder toDomainModel(CasePaymentOrderEntity casePaymentOrderEntity);

    void mergeIntoEntity(@MappingTarget CasePaymentOrderEntity target,
                         UpdateCasePaymentOrderRequest updateCasePaymentOrderRequest,
                         String createdBy);

}

