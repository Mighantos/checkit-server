package com.github.checkit.service;

import com.github.checkit.dto.GestorUserDto;
import com.github.checkit.exception.KeycloakApiAdminException;
import com.github.checkit.exception.SelfAdminRoleChangeException;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.util.KeycloakApiUtil;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    private final KeycloakApiUtil keycloakApiUtil;
    private final UserService userService;
    private final VocabularyService vocabularyService;
    private final GestoringRequestService gestoringRequestService;

    /**
     * Constructor.
     */
    public AdminUserService(KeycloakApiUtil keycloakApiUtil, UserService userService,
                            VocabularyService vocabularyService, GestoringRequestService gestoringRequestService) {
        this.keycloakApiUtil = keycloakApiUtil;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
        this.gestoringRequestService = gestoringRequestService;
    }

    /**
     * Returns all users with their gestored vocabularies.
     *
     * @return list of users
     */
    public List<GestorUserDto> getAllUsers() {
        return userService.findAll().stream().filter(user -> !user.getId().equals(keycloakApiUtil.getApiAdminId()))
            .map(user -> {
                boolean admin = keycloakApiUtil.isAdmin(user.getId());
                UserRepresentation userRepresentation =
                    keycloakApiUtil.getApi().users().get(user.getId()).toRepresentation();
                Set<URI> gestoredVocabularies = user.getGestoredVocabularies();
                return new GestorUserDto(user, userRepresentation.getEmail(), userRepresentation.getUsername(), admin,
                    gestoredVocabularies);
            }).collect(Collectors.toList());
    }

    /**
     * Sets or removes admin role for specified user in keycloak.
     *
     * @param userKeycloakId ID of user to be modified
     * @param admin          if user should have admin role or not
     */
    public void setAdminRoleToUser(String userKeycloakId, boolean admin) {
        checkNotApiAdmin(userKeycloakId);
        checkNotCurrentUser(userKeycloakId);
        User user = userService.findRequiredByUserId(userKeycloakId);
        if (admin) {
            keycloakApiUtil.setAdminRoleForUser(userKeycloakId);
            logger.info("User {} was given admin role.", user.toSimpleString());
        } else {
            keycloakApiUtil.removeAdminRoleForUser(userKeycloakId);
            logger.info("User {} was removed from admins.", user.toSimpleString());
        }
    }

    /**
     * Adds user as a gestor of the specified vocabulary.
     *
     * @param vocabularyUri vocabulary URI
     * @param userId        user's ID
     */
    public void addUserAsGestorOfVocabulary(URI vocabularyUri, String userId) {
        checkNotApiAdmin(userId);
        Vocabulary vocabulary = vocabularyService.findRequired(vocabularyUri);
        User user = userService.getRequiredReferenceByUserId(userId);
        vocabulary.addGestor(user);
        gestoringRequestService.remove(vocabularyUri, user.getUri());
        vocabularyService.update(vocabulary);
        logger.info("User {} was added as gestor of vocabulary {}.", user.toSimpleString(),
            vocabulary.toSimpleString());
    }

    /**
     * Removes user from gestors of the specified vocabulary.
     *
     * @param vocabularyUri vocabulary URI
     * @param userId        user's ID
     */
    public void removeUserAsGestorFromVocabulary(URI vocabularyUri, String userId) {
        checkNotApiAdmin(userId);
        Vocabulary vocabulary = vocabularyService.findRequired(vocabularyUri);
        User user = userService.getRequiredReferenceByUserId(userId);
        vocabulary.removeGestor(user);
        vocabularyService.update(vocabulary);
        logger.info("User {} was removed from gestors of vocabulary {}.", user.toSimpleString(),
            vocabulary.toSimpleString());
    }

    private void checkNotApiAdmin(String userId) {
        if (userId.equals(keycloakApiUtil.getApiAdminId())) {
            throw new KeycloakApiAdminException();
        }
    }

    private void checkNotCurrentUser(String userId) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userId.equals(currentUserId)) {
            throw new SelfAdminRoleChangeException();
        }
    }

    public int getAllAdminCount() {
        return keycloakApiUtil.getAdminCount();
    }
}
