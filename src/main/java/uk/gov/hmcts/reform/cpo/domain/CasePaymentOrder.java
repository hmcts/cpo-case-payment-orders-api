package uk.gov.hmcts.reform.cpo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class CasePaymentOrder {

    @JsonIgnore
    private Long id;

    private Date createdTimestamp;

    private Date effectiveFrom;

    private String caseId;

    private String caseTypeId;

    private String action;

    private String responsibleParty;

    private String orderReference;

    private String createdBy;
}
