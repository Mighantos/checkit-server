package com.github.checkit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dto.AdminPanelSummaryDto;
import com.github.checkit.environment.Generator;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.service.auxilary.BaseServiceTestRunner;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminPanelServiceTest extends BaseServiceTestRunner {

    @Autowired
    private AdminPanelService sut;

    @Autowired
    private RepositoryConfigProperties repositoryConfigProperties;
    private User gestor;
    private Vocabulary vocabulary;

    @BeforeEach
    void setUp() {
        this.gestor = Generator.generateUser("gestor", repositoryConfigProperties.getUser().getIdPrefix());
        transactional(() -> em.persist(gestor));

        this.vocabulary = Generator.generateVocabulary(Collections.singleton(gestor));
        transactional(() -> em.persist(vocabulary, descriptorFactory.vocabularyDescriptor(vocabulary)));
    }

    @Test
    void getSummary() {
        when(keycloakApiUtil.getAdminCount()).thenReturn(1);
        AdminPanelSummaryDto summary = sut.getSummary();
        assertEquals(summary.getAdminCount(), 1);
        assertEquals(summary.getVocabularyCount(), 1);
        assertEquals(summary.getVocabularyWithGestorCount(), 1);
        assertEquals(summary.getPendingGestoringRequestCount(), 0);
    }

    @Test
    void getSummaryWithNoGestor() {
        this.vocabulary.setGestors(Collections.emptySet());
        transactional(() -> em.merge(vocabulary, descriptorFactory.vocabularyDescriptor(vocabulary)));
        when(keycloakApiUtil.getAdminCount()).thenReturn(1);
        AdminPanelSummaryDto summary = sut.getSummary();
        assertEquals(summary.getVocabularyWithGestorCount(), 0);
    }
}