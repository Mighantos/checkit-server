package com.github.checkit.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.environment.Generator;
import com.github.checkit.exception.ForbiddenException;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.exception.PublicationContextWasUpdatedException;
import com.github.checkit.model.Change;
import com.github.checkit.model.Comment;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.model.auxilary.AbstractEntity;
import com.github.checkit.service.auxilary.BaseServiceTestRunner;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ChangeServiceTest extends BaseServiceTestRunner {

    @Autowired
    private ChangeService sut;

    @Autowired
    private RepositoryConfigProperties repositoryConfigProperties;

    private User user;
    private User gestor;
    private Vocabulary vocabularyWithGestor;
    private VocabularyContext vocabularyContextWithGestor;
    private Change changeWithGestor;
    private Vocabulary vocabularyWithoutGestor;
    private VocabularyContext vocabularyContextWithoutGestor;
    private Change changeWithoutGestor;
    private ProjectContext projectContext;
    private PublicationContext publicationContext;

    @BeforeEach
    void setUp() {
        this.user = Generator.generateDefaultUser(repositoryConfigProperties.getUser().getIdPrefix());
        this.gestor = Generator.generateUser("gestor", repositoryConfigProperties.getUser().getIdPrefix());
        transactional(() -> {
            em.persist(user);
            em.persist(gestor);
        });

        this.vocabularyWithGestor = Generator.generateVocabulary(Collections.singleton(gestor));
        this.vocabularyContextWithGestor = Generator.generateVocabularyContext(vocabularyWithGestor);
        this.vocabularyWithoutGestor = Generator.generateVocabulary();
        this.vocabularyContextWithoutGestor = Generator.generateVocabularyContext(vocabularyWithoutGestor);
        Set<VocabularyContext> vocabularyContexts = new HashSet<>();
        vocabularyContexts.add(vocabularyContextWithGestor);
        vocabularyContexts.add(vocabularyContextWithoutGestor);
        this.projectContext = Generator.generateProjectContext(user, vocabularyContexts);
        Set<Change> changes = new HashSet<>();
        this.changeWithGestor = Generator.generateCreateChange(vocabularyContextWithGestor);
        this.changeWithoutGestor = Generator.generateCreateChange(vocabularyContextWithoutGestor);
        changes.add(changeWithGestor);
        changes.add(changeWithoutGestor);
        this.publicationContext = Generator.generatePublicationContext(projectContext, changes);
        transactional(() -> {
            em.persist(vocabularyWithGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithGestor));
            em.persist(vocabularyWithoutGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithoutGestor));
            em.persist(vocabularyContextWithGestor,
                descriptorFactory.vocabularyDescriptor(vocabularyContextWithGestor.getUri()));
            em.persist(vocabularyContextWithoutGestor,
                descriptorFactory.vocabularyDescriptor(vocabularyContextWithoutGestor.getUri()));
            em.persist(projectContext, descriptorFactory.projectContextDescriptor(projectContext));
            em.persist(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext));
        });
    }

    @Test
    @WithMockUser
    void checkUserCanReviewChangeThrowsForbidden() {
        ForbiddenException forbiddenException =
            assertThrowsExactly(ForbiddenException.class, () -> sut.checkUserCanReviewChange(user.getUri(),
                publicationContext.getChanges().iterator().next().getUri()));
        assertTrue(forbiddenException.getMessage().contains("can't review change"));
    }

    @Test
    @WithMockUser
    void checkGestorCanReviewChange() {
        sut.checkUserCanReviewChange(gestor.getUri(), changeWithGestor.getUri());
    }

    @Test
    @WithMockUser("gestor")
    void approveChange() {
        assertFalse(changeWithGestor.isApproved(gestor));

        sut.approveChange(changeWithGestor.getId(), publicationContext.getModified());
        Change dbChange = em.find(Change.class, changeWithGestor.getUri());
        assertTrue(dbChange.isApproved(gestor));
    }

    @Test
    @WithMockUser("gestor")
    void approveChangeWithNonExistingChangeId() {
        NotFoundException notFoundException = assertThrowsExactly(NotFoundException.class,
            () -> sut.approveChange("non-existing-id", publicationContext.getModified()));
        assertTrue(notFoundException.getMessage().startsWith("Change identified by"));
    }

    @Test
    @WithMockUser("gestor")
    void approveChangeWithOutdatedTime() {
        assertThrowsExactly(PublicationContextWasUpdatedException.class,
            () -> sut.approveChange(changeWithGestor.getId(), publicationContext.getModified().minusSeconds(500)));
    }

    @Test
    @WithMockUser
    void approveChangeWithoutGestoring() {
        ForbiddenException forbiddenException = assertThrowsExactly(ForbiddenException.class,
            () -> sut.approveChange(changeWithGestor.getId(), publicationContext.getModified()));
        assertTrue(forbiddenException.getMessage().contains("can't review change"));
    }

    @Test
    @WithMockUser("gestor")
    void approveChanges() {
        vocabularyWithoutGestor.addGestor(gestor);
        transactional(
            () -> em.merge(vocabularyWithoutGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithoutGestor)));
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isApproved(gestor)));

        sut.approveChanges(changeUris, publicationContext.getModified());
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isApproved(gestor)));
    }

    @Test
    @WithMockUser("gestor")
    void approveChangesIncludingNonGestoredChange() {
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isApproved(gestor)));

        ForbiddenException forbiddenException = assertThrowsExactly(ForbiddenException.class,
            () -> sut.approveChanges(changeUris, publicationContext.getModified()));
        assertTrue(forbiddenException.getMessage().contains("can't review change"));
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isApproved(gestor)));
    }

    @Test
    @WithMockUser("gestor")
    void approveChangesWithOutdatedChanges() {
        vocabularyWithoutGestor.addGestor(gestor);
        transactional(
            () -> em.merge(vocabularyWithoutGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithoutGestor)));
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isApproved(gestor)));

        assertThrowsExactly(PublicationContextWasUpdatedException.class,
            () -> sut.approveChanges(changeUris, publicationContext.getModified().minusSeconds(500)));
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isApproved(gestor)));
    }


    @Test
    @WithMockUser("gestor")
    void approveChangesWithNonExistingChange() {
        vocabularyWithoutGestor.addGestor(gestor);
        transactional(
            () -> em.merge(vocabularyWithoutGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithoutGestor)));
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isApproved(gestor)));
        List<URI> changeUrisWithNonExisting = new ArrayList<>(changeUris);
        changeUrisWithNonExisting.add(URI.create("http://example.com/non/existing"));

        assertThrowsExactly(NotFoundException.class,
            () -> sut.approveChanges(changeUrisWithNonExisting, publicationContext.getModified()));
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isApproved(gestor)));
    }

    @Test
    @WithMockUser("gestor")
    void rejectChange() {
        assertFalse(changeWithGestor.isRejected(gestor));

        sut.rejectChange(changeWithGestor.getId(), publicationContext.getModified());
        Change dbChange = em.find(Change.class, changeWithGestor.getUri());
        assertTrue(dbChange.isRejected(gestor));
    }

    @Test
    @WithMockUser("gestor")
    void rejectChangeWithNonExistingChangeId() {
        NotFoundException notFoundException = assertThrowsExactly(NotFoundException.class,
            () -> sut.rejectChange("non-existing-id", publicationContext.getModified()));
        assertTrue(notFoundException.getMessage().startsWith("Change identified by"));
    }

    @Test
    @WithMockUser("gestor")
    void rejectChangeWithOutdatedTime() {
        assertThrowsExactly(PublicationContextWasUpdatedException.class,
            () -> sut.rejectChange(changeWithGestor.getId(), publicationContext.getModified().minusSeconds(500)));
    }

    @Test
    @WithMockUser
    void rejectChangeWithoutGestoring() {
        ForbiddenException forbiddenException = assertThrowsExactly(ForbiddenException.class,
            () -> sut.rejectChange(changeWithGestor.getId(), publicationContext.getModified()));
        assertTrue(forbiddenException.getMessage().contains("can't review change"));
    }

    @Test
    @WithMockUser("gestor")
    void rejectChanges() {
        vocabularyWithoutGestor.addGestor(gestor);
        transactional(
            () -> em.merge(vocabularyWithoutGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithoutGestor)));
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isRejected(gestor)));

        sut.rejectChanges(changeUris, publicationContext.getModified());
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isRejected(gestor)));
    }

    @Test
    @WithMockUser("gestor")
    void rejectChangesIncludingNonGestoredChange() {
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isRejected(gestor)));

        ForbiddenException forbiddenException = assertThrowsExactly(ForbiddenException.class,
            () -> sut.rejectChanges(changeUris, publicationContext.getModified()));
        assertTrue(forbiddenException.getMessage().contains("can't review change"));
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isRejected(gestor)));
    }

    @Test
    @WithMockUser("gestor")
    void rejectChangesWithOutdatedChanges() {
        vocabularyWithoutGestor.addGestor(gestor);
        transactional(
            () -> em.merge(vocabularyWithoutGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithoutGestor)));
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isRejected(gestor)));

        assertThrowsExactly(PublicationContextWasUpdatedException.class,
            () -> sut.rejectChanges(changeUris, publicationContext.getModified().minusSeconds(500)));
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isRejected(gestor)));
    }


    @Test
    @WithMockUser("gestor")
    void rejectChangesWithNonExistingChange() {
        vocabularyWithoutGestor.addGestor(gestor);
        transactional(
            () -> em.merge(vocabularyWithoutGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithoutGestor)));
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isRejected(gestor)));
        List<URI> changeUrisWithNonExisting = new ArrayList<>(changeUris);
        changeUrisWithNonExisting.add(URI.create("http://example.com/non/existing"));

        assertThrowsExactly(NotFoundException.class,
            () -> sut.rejectChanges(changeUrisWithNonExisting, publicationContext.getModified()));
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isRejected(gestor)));
    }

    @Test
    void removeChangeReview() {
    }

    @Test
    @WithMockUser("gestor")
    void removeChangesReview() {
        setChangesAsReviewed();
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isReviewed(gestor)));

        sut.removeChangesReview(changeUris, publicationContext.getModified());
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isReviewed(gestor)));
    }

    @Test
    @WithMockUser("gestor")
    void removeChangesReviewRemovesFinalComment() {
        setChangesAsReviewed();
        Comment comment = Generator.generateRejectionCommentOnChange(gestor, changeWithGestor);
        transactional(() -> em.persist(comment, descriptorFactory.commentDescriptor()));
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        assertTrue(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class).setParameter("s", comment.getUri())
            .getSingleResult());
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isReviewed(gestor)));

        sut.removeChangesReview(changeUris, publicationContext.getModified());
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertFalse(em.find(Change.class, changeUri).isReviewed(gestor)));
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class).setParameter("s", comment.getUri())
            .getSingleResult());
    }

    @Test
    @WithMockUser("gestor")
    void removeChangesReviewIncludingNonGestoredChange() {
        changeWithGestor.addApprovedBy(gestor);
        changeWithoutGestor.addRejectedBy(gestor);
        transactional(
            () -> em.merge(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext)));
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isReviewed(gestor)));

        ForbiddenException forbiddenException = assertThrowsExactly(ForbiddenException.class,
            () -> sut.removeChangesReview(changeUris, publicationContext.getModified()));
        assertTrue(forbiddenException.getMessage().contains("can't review change"));
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isReviewed(gestor)));
    }

    @Test
    @WithMockUser("gestor")
    void removeChangesReviewWithOutdatedChanges() {
        setChangesAsReviewed();
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isReviewed(gestor)));

        assertThrowsExactly(PublicationContextWasUpdatedException.class,
            () -> sut.removeChangesReview(changeUris, publicationContext.getModified().minusSeconds(500)));
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isReviewed(gestor)));
    }


    @Test
    @WithMockUser("gestor")
    void removeChangesReviewWithNonExistingChange() {
        setChangesAsReviewed();
        List<URI> changeUris = publicationContext.getChanges().stream().map(AbstractEntity::getUri).toList();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isReviewed(gestor)));
        List<URI> changeUrisWithNonExisting = new ArrayList<>(changeUris);
        changeUrisWithNonExisting.add(URI.create("http://example.com/non/existing"));

        assertThrowsExactly(NotFoundException.class,
            () -> sut.removeChangesReview(changeUrisWithNonExisting, publicationContext.getModified()));
        em.getEntityManagerFactory().getCache().evictAll();
        changeUris.forEach(changeUri -> assertTrue(em.find(Change.class, changeUri).isReviewed(gestor)));
    }

    private void setChangesAsReviewed() {
        vocabularyWithoutGestor.addGestor(gestor);
        changeWithGestor.addApprovedBy(gestor);
        changeWithoutGestor.addRejectedBy(gestor);
        transactional(() -> {
            em.merge(vocabularyWithoutGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithoutGestor));
            em.merge(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext));
        });
    }
}