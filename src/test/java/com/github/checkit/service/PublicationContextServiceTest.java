package com.github.checkit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dto.ChangeDto;
import com.github.checkit.dto.ContextChangesDto;
import com.github.checkit.dto.PublicationContextDetailDto;
import com.github.checkit.dto.PublicationContextDto;
import com.github.checkit.dto.auxiliary.PublicationContextState;
import com.github.checkit.environment.Generator;
import com.github.checkit.model.Change;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.service.auxilary.BaseServiceTestRunner;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PublicationContextServiceTest extends BaseServiceTestRunner {

    @Autowired
    private PublicationContextService sut;

    @Autowired
    private RepositoryConfigProperties repositoryConfigProperties;

    private User user;
    private User gestor;
    private Vocabulary vocabulary;
    private ProjectContext projectContext;
    private VocabularyContext vocabularyContext;
    private PublicationContext publicationContext;

    @BeforeEach
    void setUp() {
        this.user = Generator.generateDefaultUser(repositoryConfigProperties.getUser().getIdPrefix());
        this.gestor = Generator.generateUser("gestor", repositoryConfigProperties.getUser().getIdPrefix());
        transactional(() -> {
            em.persist(user);
            em.persist(gestor);
        });

        this.vocabulary = Generator.generateVocabulary(Collections.singleton(gestor));
        this.vocabularyContext = Generator.generateVocabularyContext(vocabulary);
        this.projectContext = Generator.generateProjectContext(user, Collections.singleton(
            vocabularyContext));
        Set<Change> changes = Collections.singleton(Generator.generateCreateChange(vocabularyContext));
        this.publicationContext = Generator.generatePublicationContext(projectContext, changes);
        transactional(() -> {
            em.persist(vocabulary, descriptorFactory.vocabularyDescriptor(vocabulary));
            em.persist(vocabularyContext,
                descriptorFactory.vocabularyDescriptor(vocabularyContext.getUri()));
            em.persist(projectContext, descriptorFactory.projectContextDescriptor(projectContext));
            em.persist(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext));
        });
    }

    @Test
    @WithMockUser
    void getReadonlyPublicationContexts() {
        List<PublicationContextDto> readonlyPublicationContexts = sut.getReadonlyPublicationContexts();
        assertEquals(readonlyPublicationContexts.size(), 1);
        assertEquals(readonlyPublicationContexts.get(0).getUri(), publicationContext.getUri());

        vocabulary.setGestors(Collections.singleton(user));
        transactional(() -> em.merge(vocabulary, descriptorFactory.vocabularyDescriptor(vocabulary)));
        readonlyPublicationContexts = sut.getReadonlyPublicationContexts();
        assertEquals(readonlyPublicationContexts.size(), 0);
    }

    @Test
    @WithMockUser
    void getReviewablePublicationContexts() {
        List<PublicationContextDto> reviewablePublicationContexts = sut.getReviewablePublicationContexts();
        assertEquals(reviewablePublicationContexts.size(), 0);
        vocabulary.setGestors(Collections.singleton(user));
        transactional(() -> em.merge(vocabulary, descriptorFactory.vocabularyDescriptor(vocabulary)));

        reviewablePublicationContexts = sut.getReviewablePublicationContexts();
        assertEquals(reviewablePublicationContexts.size(), 1);
        assertEquals(reviewablePublicationContexts.get(0).getUri(), publicationContext.getUri());
    }

    @Test
    @WithMockUser
    void getPublicationContextDetailWithNoChangesToReview() {
        PublicationContextDetailDto publicationContextDetail =
            sut.getPublicationContextDetail(publicationContext.getId());
        assertEquals(publicationContextDetail.getState(), PublicationContextState.WAITING_FOR_OTHERS);
    }

    @Test
    @WithMockUser("gestor")
    void getPublicationContextDetailWithOneReviewableChange() {
        PublicationContextDetailDto publicationContextDetail =
            sut.getPublicationContextDetail(publicationContext.getId());
        assertEquals(publicationContextDetail.getState(), PublicationContextState.CREATED);
    }

    @Test
    @WithMockUser
    void getPublicationContextDetailRollbackChangeDoesNotChangeStatistics() {
        PublicationContextDetailDto publicationContextDetail =
            sut.getPublicationContextDetail(publicationContext.getId());
        assertEquals(publicationContextDetail.getStatistics().getTotalChanges(), 1);

        Change rollbackedChange = Generator.generateRollbackedChange(vocabularyContext);
        rollbackedChange.addApprovedBy(user);
        publicationContext.getChanges().add(rollbackedChange);
        transactional(() ->
            em.merge(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext)));
        publicationContextDetail =
            sut.getPublicationContextDetail(publicationContext.getId());
        assertEquals(publicationContextDetail.getStatistics().getTotalChanges(), 1);
    }

    @Test
    @WithMockUser("gestor")
    void getPublicationContextDetailWithApprovedChanges() {
        publicationContext.getChanges().stream().iterator().next().addApprovedBy(gestor);
        transactional(() ->
            em.merge(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext)));
        PublicationContextDetailDto publicationContextDetail =
            sut.getPublicationContextDetail(publicationContext.getId());
        assertEquals(publicationContextDetail.getState(), PublicationContextState.APPROVABLE);
    }

    @Test
    @WithMockUser
    void getChangesInContextInPublicationContextWithHiddenRollbackChange() {
        publicationContext.getChanges().add(Generator.generateRollbackedChange(vocabularyContext));
        transactional(() ->
            em.merge(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext)));
        ContextChangesDto contextChangesDto =
            sut.getChangesInContextInPublicationContext(publicationContext.getId(), vocabulary.getUri(), "en");
        List<ChangeDto> changes = contextChangesDto.getChanges();
        assertEquals(changes.size(), 1);
    }

    @Test
    @WithMockUser
    void getChangesInContextInPublicationContextWithRollbackChange() {
        Change rollbackedChange = Generator.generateRollbackedChange(vocabularyContext);
        rollbackedChange.addApprovedBy(user);
        publicationContext.getChanges().add(rollbackedChange);
        transactional(() ->
            em.merge(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext)));
        ContextChangesDto contextChangesDto =
            sut.getChangesInContextInPublicationContext(publicationContext.getId(), vocabulary.getUri(), "en");
        List<ChangeDto> changes = contextChangesDto.getChanges();
        assertEquals(changes.size(), 2);
        assertTrue(publicationContext.getChanges().stream().map(Change::getUri).toList()
            .containsAll(changes.stream().map(ChangeDto::getUri).toList()));
    }
}