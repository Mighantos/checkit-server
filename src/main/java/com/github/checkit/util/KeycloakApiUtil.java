package com.github.checkit.util;

import com.github.checkit.config.properties.KeycloakConfigProperties;
import com.github.checkit.exception.KeycloakConfigurationException;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.security.UserRole;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ProcessingException;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakApiUtil {

    Logger logger = LoggerFactory.getLogger(KeycloakApiUtil.class);

    private final KeycloakConfigProperties keycloakConfigProperties;

    private static final String ASSEMBLY_LINE_CLIENT_PREFIX = "al-";

    @Getter
    private RealmResource api;

    @Getter
    private String clientUUID;

    @Getter
    private RoleRepresentation adminRole;

    @Getter
    private RoleRepresentation userRole;

    @Getter
    private String apiAdminId;


    public KeycloakApiUtil(KeycloakConfigProperties keycloakConfigProperties) {
        this.keycloakConfigProperties = keycloakConfigProperties;
    }

    @PostConstruct
    private void init() {
        createConnection();
        checkApiRights();
        fetchClientUUID();
        fetchClientRoles();
        fetchApiAdminId();
    }

    public boolean isAdmin(String userId) {
        return api.users().get(userId)
                .roles().clientLevel(clientUUID).listAll()
                .contains(adminRole);
    }

    private void createConnection() {
        api = KeycloakBuilder.builder()
                .serverUrl(keycloakConfigProperties.getAuthUrl())
                .realm(keycloakConfigProperties.getRealm())
                .username(keycloakConfigProperties.getApiAdmin().getUsername())
                .password(keycloakConfigProperties.getApiAdmin().getPassword())
                .clientId(keycloakConfigProperties.getApiClientId())
                .build().realm(keycloakConfigProperties.getRealm());
    }

    private void checkApiRights() {
        checkFirstConnectionAndUserSearchRight();
        fetchApiAdminId();
        UserResource apiAdminUserResource = api.users().get(apiAdminId);
        checkUserUpdateRight(apiAdminUserResource);
        checkClientSearchRight();
        checkUserClientRolesUpdateRight(apiAdminUserResource);
    }

    private void checkFirstConnectionAndUserSearchRight() {
        try {
            api.users().searchByUsername(keycloakConfigProperties.getApiAdmin().getUsername(), true);
        } catch (Exception e) {
            if (e instanceof ProcessingException || e instanceof NotAuthorizedException) {
                throw new KeycloakConfigurationException("HTTP 401 Unauthorized. Could not connect to Keycloak API with user %s and given password.", keycloakConfigProperties.getApiAdmin().getUsername());
            } else if (e instanceof ForbiddenException) {
                throw new KeycloakConfigurationException("HTTP 403 Forbidden. Bad API Admin user (%s) configuration. Could not search in users by username.", keycloakConfigProperties.getApiAdmin().getUsername());
            } else if (!(e instanceof KeycloakConfigurationException)) {
                e.printStackTrace();
                throw new KeycloakConfigurationException("Bad Keycloak API configuration. serverUrl: %s, realm: %s, username: %s, password: ****, clientId: %s", keycloakConfigProperties.getAuthUrl(), keycloakConfigProperties.getRealm(), keycloakConfigProperties.getApiAdmin().getUsername(), keycloakConfigProperties.getApiClientId());
            }
        }
    }

    private void checkUserUpdateRight(UserResource apiAdminUserResource) {
        UserRepresentation apiAdminUserRepresentation = apiAdminUserResource.toRepresentation();
        apiAdminUserRepresentation.setFirstName("API Admin for CheckIt");
        apiAdminUserRepresentation.setLastName("!!DO NOT REMOVE THIS USER!!");
        apiAdminUserRepresentation.setEmail("do.not.remove.this.user@notemail.com");
        try {
            apiAdminUserResource.update(apiAdminUserRepresentation);
        } catch (ForbiddenException e) {
            throw new KeycloakConfigurationException("HTTP 403 Forbidden. Bad API Admin user (%s) configuration. Could not update user.", keycloakConfigProperties.getApiAdmin().getUsername());
        }
    }

    private void checkClientSearchRight() {
        try {
            List<ClientRepresentation> allClients = api.clients().findAll();
            if (allClients.isEmpty())
                throw new KeycloakConfigurationException("HTTP 403 Forbidden. Bad API Admin user (%s) configuration. Could not search for clients.", keycloakConfigProperties.getApiAdmin().getUsername());
        } catch (ForbiddenException e) {
            throw new KeycloakConfigurationException("HTTP 403 Forbidden. Bad API Admin user (%s) configuration. Could not search for clients.", keycloakConfigProperties.getApiAdmin().getUsername());
        }

    }

    private void checkUserClientRolesUpdateRight(UserResource apiAdminUserResource) {
        // Checks that user client roles can be changed for all assembly line clients (prefixed with "al-")
        try {
            Map<String, ClientMappingsRepresentation> clientMappings = apiAdminUserResource.roles().getAll().getClientMappings();
            for (String clientId : clientMappings.keySet()) {
                if (!clientId.startsWith(ASSEMBLY_LINE_CLIENT_PREFIX))
                    continue;
                List<ClientRepresentation> foundClients = api.clients().findByClientId(clientId);
                if (foundClients.isEmpty()) {
                    logger.warn("Could not find client with clientId {}", clientId);
                    continue;
                }
                RoleScopeResource roleScopeResource = apiAdminUserResource.roles().clientLevel(foundClients.get(0).getId());
                roleScopeResource.remove(roleScopeResource.listAll());
            }
        } catch (ForbiddenException e) {
            throw new KeycloakConfigurationException("HTTP 403 Forbidden. Bad API Admin user (%s) configuration. Could not update user client roles.", keycloakConfigProperties.getApiAdmin().getUsername());
        }
    }

    private void fetchClientUUID() {
        List<ClientRepresentation> clientRepresentations = api.clients().findByClientId(keycloakConfigProperties.getClientId());
        if (clientRepresentations.isEmpty())
            throw new NotFoundException("Client with id %s not found in Keycloak realm %s on %s.", keycloakConfigProperties.getClientId(), keycloakConfigProperties.getRealm(), keycloakConfigProperties.getAuthUrl());
        clientUUID = clientRepresentations.get(0).getId();
    }

    private void fetchClientRoles() {
        List<String> requiredRoles = UserRole.getRequiredRoles();
        List<RoleRepresentation> roleList = api.clients().get(clientUUID).roles().list();
        for (RoleRepresentation rr : roleList) {
            if (rr.getName().equals(UserRole.USER)) {
                userRole = rr;
                requiredRoles.remove(rr.getName());
            } else if (rr.getName().equals(UserRole.ADMIN)) {
                adminRole = rr;
                requiredRoles.remove(rr.getName());
            }
        }
        if (!requiredRoles.isEmpty()) {
            throw new KeycloakConfigurationException("Keycloak on %s in realm %s is missing required role(s) %s in client %s.", keycloakConfigProperties.getAuthUrl(), keycloakConfigProperties.getRealm(), requiredRoles, keycloakConfigProperties.getClientId());
        }
    }

    private void fetchApiAdminId() {
        List<UserRepresentation> userRepresentations = api.users().searchByUsername(keycloakConfigProperties.getApiAdmin().getUsername(), true);
        if (userRepresentations.isEmpty())
            throw new KeycloakConfigurationException("Could not find API admin %s among users.", keycloakConfigProperties.getApiAdmin().getUsername());
        apiAdminId = userRepresentations.get(0).getId();
    }
}
