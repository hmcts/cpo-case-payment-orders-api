package uk.gov.hmcts.reform.cpo.config;

import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

import static org.hibernate.event.spi.EventType.POST_DELETE;

@Configuration
public class HibernateEventListenerRegistryConfiguration {

    @Autowired
    EntityManagerFactory entityManagerFactory;

    /**
     * <p>Overrides the hibernate POST DELETE event listener, with an implementation that does nothing.</p>
     *
     * <p>This ensures Envers does not get triggered when entities are deleted, and so the audit table
     * is not updated with audit entries relating to entity deletion.</p>
     *
     * <p>On deletion of an entity, all audit related entries in the audit table are deleted by calls to
     *
     * <ul>
     *     <li>{@code void deleteAuditEntriesByUuids(List<UUID> uuids)}</li>
     *     <li>{@code void deleteAuditEntriesByCaseIds(List<Long> caseIds)}</li>
     * </ul>
     *
     * <p>on the {@code CasePaymentOrdersRepository} by the {@code CasePaymentOrdersServiceImpl}</p>
     */
    @PostConstruct
    public void eventListenerRegistry() {
        ServiceRegistryImplementor serviceRegistry = entityManagerFactory
                .unwrap(SessionFactoryImpl.class)
                .getServiceRegistry();
        final EnversService enversService = serviceRegistry.getService(EnversService.class);
        EventListenerRegistry listenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);

        listenerRegistry.setListeners(POST_DELETE, new EnversDeleteEventListenerImpl(enversService));
    }
}
