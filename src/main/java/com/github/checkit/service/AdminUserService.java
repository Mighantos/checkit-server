package com.github.checkit.service;

import com.github.checkit.util.KeycloakApiUtil;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {

    private final KeycloakApiUtil keycloakApiUtil;

    public AdminUserService(KeycloakApiUtil keycloakApiUtil) {
        this.keycloakApiUtil = keycloakApiUtil;
    }

    public void setAdminRoleToUser(String userKeycloakId, boolean admin) {
        UserResource user = keycloakApiUtil.getApi().users().get(userKeycloakId);
        RoleScopeResource roleScopeResource = user.roles().clientLevel(keycloakApiUtil.getClientUUID());
        if (admin) {
            roleScopeResource.add(List.of(keycloakApiUtil.getAdminRole()));
        } else {
            roleScopeResource.remove(List.of(keycloakApiUtil.getAdminRole()));
        }
    }
}
