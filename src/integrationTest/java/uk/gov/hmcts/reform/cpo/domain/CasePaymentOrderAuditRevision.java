package uk.gov.hmcts.reform.cpo.domain;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;

@Builder
@Getter
public class CasePaymentOrderAuditRevision {

    private RevisionType revisionType;

    private DefaultRevisionEntity revisionEntity;

    private CasePaymentOrderEntity entity;

}
