package com.github.checkit.util;

import com.github.checkit.config.properties.KeycloakConfigProperties;
import com.github.checkit.exception.KeycloakConfigurationException;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.security.UserRole;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycloakApiUtil {

    Logger logger = LoggerFactory.getLogger(KeycloakApiUtil.class);

    private final KeycloakConfigProperties keycloakConfigProperties;

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
        fetchClientUUID();
        fetchClientRoles();
        fetchApiAdminId();
    }

    private void createConnection() {
        api = KeycloakBuilder.builder()
                .serverUrl(keycloakConfigProperties.getAuthUrl())
                .realm(keycloakConfigProperties.getRealm())
                .username(keycloakConfigProperties.getAdminApi().getUsername())
                .password(keycloakConfigProperties.getAdminApi().getPassword())
                .clientId(keycloakConfigProperties.getApiClientId())
                .build().realm(keycloakConfigProperties.getRealm());
    }

    private void fetchClientUUID() {
        List<ClientRepresentation> clientRepresentations;
        try {
            clientRepresentations = api.clients().findByClientId(keycloakConfigProperties.getClientId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new KeycloakConfigurationException(String.format("Bad Keycloak API configuration. serverUrl: %s, realm: %s, username: %s, password: ****, clientId: %s", keycloakConfigProperties.getAuthUrl(), keycloakConfigProperties.getRealm(), keycloakConfigProperties.getAdminApi().getUsername(), keycloakConfigProperties.getApiClientId()));
        }
        if (clientRepresentations.isEmpty())
            throw new NotFoundException(String.format("Client with id %s not found in Keycloak realm %s on %s.", keycloakConfigProperties.getClientId(), keycloakConfigProperties.getRealm(), keycloakConfigProperties.getAuthUrl()));
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
            throw new KeycloakConfigurationException(String.format("Keycloak on %s in realm %s is missing required role(s) %s in client %s.", keycloakConfigProperties.getAuthUrl(), keycloakConfigProperties.getRealm(), requiredRoles, keycloakConfigProperties.getClientId()));
        }
    }

    private void fetchApiAdminId() {
        List<UserRepresentation> userRepresentations = api.users().searchByUsername(keycloakConfigProperties.getAdminApi().getUsername(), true);
        try {
            apiAdminId = userRepresentations.get(0).getId();
        } catch (Exception e) {
            logger.info("Could not find API admin {} among users.", keycloakConfigProperties.getAdminApi().getUsername());
        }
    }
}
