package uk.gov.hmcts.reform.cpo.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CasePaymentOrderMapper {

    CasePaymentOrderEntity toEntity(CasePaymentOrder casePaymentOrder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdTimestamp", ignore = true)
    CasePaymentOrderEntity toEntity(CreateCasePaymentOrderRequest createCasePaymentOrderRequest,
                                                String createdBy);

    CasePaymentOrder toDomainModel(CasePaymentOrderEntity casePaymentOrderEntity);

    @Mapping(target = "createdTimestamp", expression = "java(java.time.LocalDateTime.now())")
    void mergeIntoEntity(@MappingTarget CasePaymentOrderEntity target,
                         UpdateCasePaymentOrderRequest updateCasePaymentOrderRequest,
                         String createdBy);

    List<CasePaymentOrder> toDomainModelList(List<CasePaymentOrderEntity> casePaymentOrderEntities);
}

