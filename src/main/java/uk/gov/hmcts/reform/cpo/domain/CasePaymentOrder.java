package uk.gov.hmcts.reform.cpo.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class CasePaymentOrder {

    private UUID id;

    private LocalDateTime createdTimestamp;

    private LocalDateTime effectiveFrom;

    private Long caseId;

    private String action;

    private String responsibleParty;

    private String orderReference;

    private String createdBy;
}
