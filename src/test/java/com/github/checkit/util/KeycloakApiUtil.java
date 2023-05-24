package com.github.checkit.util;

import java.util.List;
import org.apache.jena.atlas.lib.NotImplemented;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.context.annotation.Primary;

@Primary
public class KeycloakApiUtil {

    public RealmResource getApi() {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }

    public String getClientUUID() {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }

    public RoleRepresentation getAdminRole() {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }

    public RoleRepresentation getUserRole() {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }

    public String getApiAdminId() {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }

    public boolean isAdmin(String userId) {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }

    public void setAdminRoleForUser(String userId) {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }

    public void removeAdminRoleForUser(String userId) {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }

    public int getAdminCount() {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }

    public List<String> getAdminIds() {
        throw new NotImplemented("Not implemented for tests. Mock the behavior.");
    }
}
