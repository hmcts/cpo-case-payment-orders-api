package uk.gov.hmcts.reform.cpo.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "case_payment_orders")
@Audited(withModifiedFlag = true)
public class CasePaymentOrderEntity {

    public static final String UNIQUE_CASE_ID_ORDER_REF_CONSTRAINT = "unique_case_id_order_reference";
    public static final String CASE_ID = "caseId";
    public static final String ORDER_REFERENCE = "orderReference";

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    @Column(length = 16, nullable = false)
    private Long caseId;

    @Column(length = 70, nullable = false)
    private String action;

    @Column(length = 1024, nullable = false)
    private String responsibleParty;

    @Column(length = 70, nullable = false)
    private String orderReference;

    @Column(length = 70)
    private String createdBy;

    private boolean historyExists;

}
