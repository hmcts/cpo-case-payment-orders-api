package uk.gov.hmcts.reform.cpo.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "case_payment_orders")
public class CasePaymentOrderEntity {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    @Column(nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(length = 16, nullable = false)
    private Long caseId;

    @Column(length = 70, nullable = false)
    private String caseTypeId;

    @Column(length = 70, nullable = false)
    private String action;

    @Column(length = 1024, nullable = false)
    private String responsibleParty;

    @Column(length = 70, nullable = false)
    private String orderReference;

    @Column(length = 70)
    private String createdBy;
}
