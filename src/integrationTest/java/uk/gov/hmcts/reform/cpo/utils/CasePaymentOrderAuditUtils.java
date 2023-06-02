package uk.gov.hmcts.reform.cpo.utils;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrderAuditRevision;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CasePaymentOrderAuditUtils {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final AuditReader auditReader;

    public static final boolean SELECT_DELETED_ENTITIES = true;
    public static final boolean SELECT_ENTITIES_ONLY = true;
    public static final boolean SELECT_REVISION_INFO_ONLY = false;

    public CasePaymentOrderAuditUtils(EntityManagerFactory entityManagerFactory) {
        this.auditReader = AuditReaderFactory.get(entityManagerFactory.createEntityManager());
    }

    public AuditQuery getAuditQuery(UUID id, boolean shouldSelectEntitiesOnly) {
        return auditReader
            .createQuery()
            .forRevisionsOfEntity(
                CasePaymentOrderEntity.class,
                shouldSelectEntitiesOnly,
                SELECT_DELETED_ENTITIES)
            .add(AuditEntity.id().eq(id));
    }

    public List<CasePaymentOrderAuditRevision> getAuditRevisions(UUID id) {
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = getAuditQuery(id, SELECT_REVISION_INFO_ONLY).getResultList();

        return resultList.stream()
            .map(array -> CasePaymentOrderAuditRevision
                .builder()
                .entity((CasePaymentOrderEntity)array[0])
                .revisionEntity((DefaultRevisionEntity)array[1])
                .revisionType((RevisionType)array[2])
                .build())
            .collect(Collectors.toList());
    }

}
