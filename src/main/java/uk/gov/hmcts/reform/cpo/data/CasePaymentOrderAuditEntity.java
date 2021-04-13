package uk.gov.hmcts.reform.cpo.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "case_payment_orders_audit")
public class CasePaymentOrderAuditEntity implements Serializable {

    private static final long serialVersionUID = 3201769457154562891L;

    @NonNull
    @Id
    private UUID id;

    @Column(name = "case_id")
    private Long caseId;
}

