package uk.gov.hmcts.reform.cpo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Component
@Validated
@ConfigurationProperties(prefix = "s2s")
public class ServiceAuthorizationConfig {

    private Map<String, String> permissions;

    public boolean hasPermissions(String serviceId, Permission crudPermission) {
        return permissions.containsKey(serviceId) && permissions.get(serviceId).contains(crudPermission.getLabel());
    }

    public void setAuthorizations(Map<String, @Valid ServicePermissionInfo> authorizations) {
        permissions = authorizations
                .values()
                .stream()
                .collect(toMap(ServicePermissionInfo::getId, ServicePermissionInfo::getPermission));
    }
}

