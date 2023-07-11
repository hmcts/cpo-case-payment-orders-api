package uk.gov.hmcts.reform.cpo.repository;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@NoArgsConstructor
@Data
@Table(name = "revinfo")
class RevInfo {
    @Id
    private int rev;
}
