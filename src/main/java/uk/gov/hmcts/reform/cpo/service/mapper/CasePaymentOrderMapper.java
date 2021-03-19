package uk.gov.hmcts.reform.cpo.service.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.cpo.payload.CasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CasePaymentOrderMapper {

    CasePaymentOrderEntity toEntity(CasePaymentOrder casePaymentOrder);

    CasePaymentOrder toDomainModel(CasePaymentOrderEntity casePaymentOrderEntity);

    CasePaymentOrder toRequest(CasePaymentOrderRequest casePaymentOrderRequest, String createdBy,
                               LocalDateTime createdTimestamp);
}
