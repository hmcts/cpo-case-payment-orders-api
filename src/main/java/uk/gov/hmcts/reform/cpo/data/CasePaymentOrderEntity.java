package uk.gov.hmcts.reform.cpo.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "case_payment_orders")
public class CasePaymentOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_timestamp")
    private Date createdTimestamp;

    @Column(name = "effective_from", nullable = false)
    private Date effectiveFrom;

    @Column(name = "case_id", length = 16, nullable = false)
    private String caseId;

    @Column(name = "casetype_id", length = 70, nullable = false)
    private String caseTypeId;

    @Column(length = 70, nullable = false)
    private String action;

    @Column(name = "responsible_party", length = 1024, nullable = false)
    private String responsibleParty;

    @Column(name = "order_reference", length = 70, nullable = false)
    private String orderReference;

    @Column(name = "created_by", length = 70)
    private String createdBy;
}
