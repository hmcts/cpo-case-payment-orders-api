package uk.gov.hmcts.reform.cpo.config;

import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.event.spi.EnversPostDeleteEventListenerImpl;
import org.hibernate.event.spi.PostDeleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnversDeleteEventListenerImpl extends EnversPostDeleteEventListenerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(EnversDeleteEventListenerImpl.class);

    public EnversDeleteEventListenerImpl(EnversService enversService) {
        super(enversService);
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        LOG.debug("PostDeleteEvent received and ignored");
    }
}