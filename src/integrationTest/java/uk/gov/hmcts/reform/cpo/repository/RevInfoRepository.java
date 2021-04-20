package uk.gov.hmcts.reform.cpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevInfoRepository extends JpaRepository<RevInfo, Integer> {
}
