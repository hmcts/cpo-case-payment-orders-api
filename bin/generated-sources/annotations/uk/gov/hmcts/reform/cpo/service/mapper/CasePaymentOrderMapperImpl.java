package uk.gov.hmcts.reform.cpo.service.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-07-10T15:36:48+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.35.0.v20230622-1425, environment: Java 17.0.7 (Homebrew)"
)
@Component
public class CasePaymentOrderMapperImpl implements CasePaymentOrderMapper {

    @Override
    public CasePaymentOrderEntity toEntity(CasePaymentOrder casePaymentOrder) {
        if ( casePaymentOrder == null ) {
            return null;
        }

        CasePaymentOrderEntity casePaymentOrderEntity = new CasePaymentOrderEntity();

        casePaymentOrderEntity.setAction( casePaymentOrder.getAction() );
        casePaymentOrderEntity.setCaseId( casePaymentOrder.getCaseId() );
        casePaymentOrderEntity.setCreatedBy( casePaymentOrder.getCreatedBy() );
        casePaymentOrderEntity.setCreatedTimestamp( casePaymentOrder.getCreatedTimestamp() );
        casePaymentOrderEntity.setHistoryExists( casePaymentOrder.isHistoryExists() );
        casePaymentOrderEntity.setOrderReference( casePaymentOrder.getOrderReference() );
        casePaymentOrderEntity.setResponsibleParty( casePaymentOrder.getResponsibleParty() );

        return casePaymentOrderEntity;
    }

    @Override
    public CasePaymentOrderEntity toEntity(CreateCasePaymentOrderRequest createCasePaymentOrderRequest, String createdBy) {
        if ( createCasePaymentOrderRequest == null && createdBy == null ) {
            return null;
        }

        CasePaymentOrderEntity casePaymentOrderEntity = new CasePaymentOrderEntity();

        if ( createCasePaymentOrderRequest != null ) {
            casePaymentOrderEntity.setAction( createCasePaymentOrderRequest.getAction() );
            if ( createCasePaymentOrderRequest.getCaseId() != null ) {
                casePaymentOrderEntity.setCaseId( Long.parseLong( createCasePaymentOrderRequest.getCaseId() ) );
            }
            casePaymentOrderEntity.setOrderReference( createCasePaymentOrderRequest.getOrderReference() );
            casePaymentOrderEntity.setResponsibleParty( createCasePaymentOrderRequest.getResponsibleParty() );
        }
        casePaymentOrderEntity.setCreatedBy( createdBy );

        return casePaymentOrderEntity;
    }

    @Override
    public CasePaymentOrder toDomainModel(CasePaymentOrderEntity casePaymentOrderEntity) {
        if ( casePaymentOrderEntity == null ) {
            return null;
        }

        CasePaymentOrder.CasePaymentOrderBuilder casePaymentOrder = CasePaymentOrder.builder();

        casePaymentOrder.action( casePaymentOrderEntity.getAction() );
        casePaymentOrder.caseId( casePaymentOrderEntity.getCaseId() );
        casePaymentOrder.createdBy( casePaymentOrderEntity.getCreatedBy() );
        casePaymentOrder.createdTimestamp( casePaymentOrderEntity.getCreatedTimestamp() );
        casePaymentOrder.historyExists( casePaymentOrderEntity.isHistoryExists() );
        casePaymentOrder.id( casePaymentOrderEntity.getId() );
        casePaymentOrder.orderReference( casePaymentOrderEntity.getOrderReference() );
        casePaymentOrder.responsibleParty( casePaymentOrderEntity.getResponsibleParty() );

        return casePaymentOrder.build();
    }

    @Override
    public void mergeIntoEntity(CasePaymentOrderEntity target, UpdateCasePaymentOrderRequest updateCasePaymentOrderRequest, String createdBy) {
        if ( updateCasePaymentOrderRequest == null && createdBy == null ) {
            return;
        }

        if ( updateCasePaymentOrderRequest != null ) {
            target.setAction( updateCasePaymentOrderRequest.getAction() );
            if ( updateCasePaymentOrderRequest.getCaseId() != null ) {
                target.setCaseId( Long.parseLong( updateCasePaymentOrderRequest.getCaseId() ) );
            }
            else {
                target.setCaseId( null );
            }
            target.setOrderReference( updateCasePaymentOrderRequest.getOrderReference() );
            target.setResponsibleParty( updateCasePaymentOrderRequest.getResponsibleParty() );
        }
        target.setCreatedBy( createdBy );
    }
}
