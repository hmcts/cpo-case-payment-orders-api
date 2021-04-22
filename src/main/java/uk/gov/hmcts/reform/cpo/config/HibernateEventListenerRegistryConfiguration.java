package uk.gov.hmcts.reform.cpo.config;

import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

import static org.hibernate.event.spi.EventType.POST_DELETE;

@Configuration
public class HibernateEventListenerRegistryConfiguration {

    @Autowired
    EntityManagerFactory entityManagerFactory;

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
