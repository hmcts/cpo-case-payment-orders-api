package uk.gov.hmcts.reform.cpo.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@EqualsAndHashCode
public class CasePaymentOrder {

    private UUID id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdTimestamp;

    private Long caseId;

    private String action;

    private String responsibleParty;

    private String orderReference;

    private String createdBy;

    private boolean historyExists;
}
