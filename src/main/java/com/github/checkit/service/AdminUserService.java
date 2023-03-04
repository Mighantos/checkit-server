package com.github.checkit.service;

import com.github.checkit.dto.GestorUserDto;
import com.github.checkit.exception.KeycloakApiAdminException;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.util.KeycloakApiUtil;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final KeycloakApiUtil keycloakApiUtil;
    private final UserService userService;
    private final VocabularyService vocabularyService;

    public AdminUserService(KeycloakApiUtil keycloakApiUtil, UserService userService, VocabularyService vocabularyService) {
        this.keycloakApiUtil = keycloakApiUtil;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
    }

    public void setAdminRoleToUser(String userKeycloakId, boolean admin) {
        checkNotApiAdmin(userKeycloakId);
        UserResource user = keycloakApiUtil.getApi().users().get(userKeycloakId);
        RoleScopeResource roleScopeResource = user.roles().clientLevel(keycloakApiUtil.getClientUUID());
        if (admin) {
            roleScopeResource.add(List.of(keycloakApiUtil.getAdminRole()));
        } else {
            roleScopeResource.remove(List.of(keycloakApiUtil.getAdminRole()));
        }
    }

    public List<GestorUserDto> getAllUsers() {
        return userService.findAll().stream().filter(user -> !user.getId().equals(keycloakApiUtil.getApiAdminId()))
                .map(user -> {
                    boolean admin = keycloakApiUtil.isAdmin(user.getId());
                    UserRepresentation userRepresentation = keycloakApiUtil.getApi().users().get(user.getId()).toRepresentation();
                    Set<URI> gestoredVocabularies = user.getGestoredVocabularies();
                    return new GestorUserDto(user, userRepresentation.getEmail(), userRepresentation.getUsername(), admin, gestoredVocabularies);
                }).collect(Collectors.toList());
    }

    private void checkNotApiAdmin(String userId) {
        if (userId.equals(keycloakApiUtil.getApiAdminId())) {
            throw new KeycloakApiAdminException();
        }
    }

    public void addUserAsGestorOfVocabulary(URI vocabularyUri, String userId) {
        checkNotApiAdmin(userId);
        Vocabulary vocabulary = vocabularyService.findRequired(vocabularyUri);
        User user = userService.getRequiredReferenceByUserId(userId);
        vocabulary.addGestor(user);
        vocabularyService.update(vocabulary);
    }

    public void removeUserAsGestorFromVocabulary(URI vocabularyUri, String userId) {
        checkNotApiAdmin(userId);
        Vocabulary vocabulary = vocabularyService.findRequired(vocabularyUri);
        User user = userService.getRequiredReferenceByUserId(userId);
        vocabulary.removeGestor(user);
        vocabularyService.update(vocabulary);
    }
}
