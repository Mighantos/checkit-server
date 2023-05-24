package com.github.checkit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.environment.Generator;
import com.github.checkit.exception.SelfAdminRoleChangeException;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.service.auxilary.BaseServiceTestRunner;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminUserServiceTest extends BaseServiceTestRunner {

    @Autowired
    private AdminUserService sut;

    @Autowired
    private RepositoryConfigProperties repositoryConfigProperties;

    private User user;
    private User gestor;
    private User admin;
    private Vocabulary vocabulary;

    @BeforeEach
    void setUp() {
        this.user = Generator.generateDefaultUser(repositoryConfigProperties.getUser().getIdPrefix());
        this.gestor = Generator.generateUser("gestor", repositoryConfigProperties.getUser().getIdPrefix());
        this.admin = Generator.generateUser("admin", repositoryConfigProperties.getUser().getIdPrefix());
        transactional(() -> {
            em.persist(user);
            em.persist(gestor);
            em.persist(admin);
        });

        this.vocabulary = Generator.generateVocabulary(Collections.singleton(gestor));
        transactional(() -> em.persist(vocabulary, descriptorFactory.vocabularyDescriptor(vocabulary)));
    }

    @Test
    @WithMockUser
    void changeAdminRoleOnCurrentUser() {
        when(keycloakApiUtil.getApiAdminId()).thenReturn("ApiAdminUser");
        assertThrows(SelfAdminRoleChangeException.class, () -> sut.setAdminRoleToUser(user.getId(), true));
    }

    @Test
    @WithMockUser
    void changeAdminRoleToDifferentUser() {
        when(keycloakApiUtil.getApiAdminId()).thenReturn("ApiAdminUser");
        sut.setAdminRoleToUser(gestor.getId(), true);
    }

    @Test
    void addUserAsGestorOfVocabulary() {
        Vocabulary dbVoc;
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 1);
        assertEquals(dbVoc.getGestors().iterator().next(), gestor);

        when(keycloakApiUtil.getApiAdminId()).thenReturn("ApiAdminUser");
        sut.addUserAsGestorOfVocabulary(vocabulary.getUri(), user.getId());
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 2);
        assertTrue(dbVoc.getGestors().contains(user));
        assertTrue(dbVoc.getGestors().contains(gestor));
    }

    @Test
    void addUserAsGestorOfVocabularyThatIsAlreadyAGestor() {
        Vocabulary dbVoc;
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 1);
        assertEquals(dbVoc.getGestors().iterator().next(), gestor);

        when(keycloakApiUtil.getApiAdminId()).thenReturn("ApiAdminUser");
        sut.addUserAsGestorOfVocabulary(vocabulary.getUri(), gestor.getId());
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 1);
    }

    @Test
    void addUserAsGestorOfVocabularyRemovesGestoringRequest() {
        Vocabulary dbVoc;
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 1);
        assertEquals(dbVoc.getGestors().iterator().next(), gestor);

        when(keycloakApiUtil.getApiAdminId()).thenReturn("ApiAdminUser");
        sut.addUserAsGestorOfVocabulary(vocabulary.getUri(), gestor.getId());
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 1);
    }

    @Test
    void removeUserAsGestorFromVocabularyThatIsNotAGestor() {
        Vocabulary dbVoc;
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 1);
        assertEquals(dbVoc.getGestors().iterator().next(), gestor);

        when(keycloakApiUtil.getApiAdminId()).thenReturn("ApiAdminUser");
        sut.removeUserAsGestorFromVocabulary(vocabulary.getUri(), user.getId());
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 1);
    }

    @Test
    void removeUserAsGestorFromVocabularyThatIsAGestor() {
        Vocabulary dbVoc;
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 1);
        assertEquals(dbVoc.getGestors().iterator().next(), gestor);

        when(keycloakApiUtil.getApiAdminId()).thenReturn("ApiAdminUser");
        sut.removeUserAsGestorFromVocabulary(vocabulary.getUri(), gestor.getId());
        dbVoc = em.find(Vocabulary.class, vocabulary.getUri());
        assertEquals(dbVoc.getGestors().size(), 0);
    }
}