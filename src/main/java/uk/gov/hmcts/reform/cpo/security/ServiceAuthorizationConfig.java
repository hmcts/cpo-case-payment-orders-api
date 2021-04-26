package uk.gov.hmcts.reform.cpo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "service-authorization")
public class ServiceAuthorizationConfig {

    private Map<String, String> permissions;

    public ServiceAuthorizationConfig(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermissions(String serviceId, Permission crudPermission) {
        return permissions.containsKey(serviceId) && permissions.get(serviceId).contains(crudPermission.getLabel());
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }
}

