package uk.gov.hmcts.reform.cpo.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class CasePaymentOrder {

    private UUID id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-ddTHH:mm:ssZ")
    private LocalDateTime createdTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-ddTHH:mm:ssZ")
    private LocalDateTime effectiveFrom;

    private Long caseId;

    private String caseTypeId;

    private String action;

    private String responsibleParty;

    private String orderReference;

    private String createdBy;
}
