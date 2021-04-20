package uk.gov.hmcts.reform.cpo.repository;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@Data
@Table(name = "revinfo")
class RevInfo {
    @Id
    private int rev;
}
