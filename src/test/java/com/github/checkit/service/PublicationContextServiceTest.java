package com.github.checkit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.checkit.config.properties.ApplicationConfigProperties;
import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dto.ChangeDto;
import com.github.checkit.dto.CommentDto;
import com.github.checkit.dto.ContextChangesDto;
import com.github.checkit.dto.PublicationContextDetailDto;
import com.github.checkit.dto.PublicationContextDto;
import com.github.checkit.dto.auxiliary.PublicationContextState;
import com.github.checkit.environment.Generator;
import com.github.checkit.exception.AlreadyExistsException;
import com.github.checkit.exception.ForbiddenException;
import com.github.checkit.exception.NotApprovableException;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.exception.RejectionCommentTooShortException;
import com.github.checkit.model.Change;
import com.github.checkit.model.Comment;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.model.auxilary.CommentTag;
import com.github.checkit.service.auxilary.BaseServiceTestRunner;
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
class PublicationContextServiceTest extends BaseServiceTestRunner {

    @Autowired
    private PublicationContextService sut;

    @Autowired
    private RepositoryConfigProperties repositoryConfigProperties;

    @Autowired
    private ApplicationConfigProperties applicationConfigProperties;

    private User user;
    private User gestor;
    private Vocabulary vocabulary;
    private VocabularyContext vocabularyContext;
    private ProjectContext projectContext;
    private Change change;
    private PublicationContext publicationContext;

    @BeforeEach
    void setUp() {
        this.user = Generator.generateDefaultUser(repositoryConfigProperties.getUser().getIdPrefix());
        this.gestor = Generator.generateUser("gestor", repositoryConfigProperties.getUser().getIdPrefix());
        transactional(() -> {
            em.persist(user);
            em.persist(gestor);
        });

        Set<User> gestors = new HashSet<>(Collections.singleton(gestor));
        this.vocabulary = Generator.generateVocabulary(gestors);
        this.vocabularyContext = Generator.generateVocabularyContext(vocabulary);
        this.projectContext = Generator.generateProjectContext(user, Collections.singleton(
            vocabularyContext));
        this.change = Generator.generateCreateChange(vocabularyContext);
        Set<Change> changes = Collections.singleton(change);
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

    @Test
    @WithMockUser("gestor")
    void approvePublicationContext() {
        change.addApprovedBy(gestor);
        transactional(() -> em.merge(change, descriptorFactory.changeDescriptor(publicationContext.getUri())));
        em.getEntityManagerFactory().getCache().evictAll();
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.APPROVABLE);

        String finalMessage = "Final message for a publication context.";
        sut.approvePublicationContext(publicationContext.getId(), finalMessage);
        PublicationContextDetailDto publicationContextDetail =
            sut.getPublicationContextDetail(publicationContext.getId());
        assertEquals(publicationContextDetail.getState(), PublicationContextState.APPROVED);
        CommentDto finalComment = publicationContextDetail.getFinalComment();
        assertNotNull(finalComment);
        assertEquals(finalComment.getAuthor().getId(), gestor.getId());
        assertEquals(finalComment.getTopic(), publicationContext.getUri());
        assertEquals(finalComment.getTag(), CommentTag.APPROVAL);
        assertEquals(finalComment.getContent(), finalMessage);
    }

    @Test
    @WithMockUser
    void approvePublicationContextWithoutPermission() {
        change.addApprovedBy(gestor);
        transactional(() -> em.merge(change, descriptorFactory.changeDescriptor(publicationContext.getUri())));
        em.getEntityManagerFactory().getCache().evictAll();
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.WAITING_FOR_OTHERS);

        String finalMessage = "Final message for a publication context.";
        ForbiddenException forbiddenException = assertThrowsExactly(ForbiddenException.class,
            () -> sut.approvePublicationContext(publicationContext.getId(), finalMessage));
        assertTrue(forbiddenException.getMessage().contains("can't review publication context"));
    }

    @Test
    @WithMockUser("gestor")
    void approvePublicationContextWithUnReviewedChange() {
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.CREATED);

        String finalMessage = "Final message for a publication context.";
        assertThrowsExactly(NotApprovableException.class,
            () -> sut.approvePublicationContext(publicationContext.getId(), finalMessage));
    }

    @Test
    @WithMockUser("gestor")
    void approvePublicationContextWithoutAVocabularyReviewed() {
        change.addApprovedBy(user);
        vocabulary.addGestor(user);
        transactional(() -> {
            em.merge(change, descriptorFactory.changeDescriptor(publicationContext.getUri()));
            em.merge(vocabulary, descriptorFactory.vocabularyDescriptor(vocabulary));
        });
        em.getEntityManagerFactory().getCache().evictAll();
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.CREATED);

        String finalMessage = "Final message for a publication context.";
        assertThrowsExactly(NotApprovableException.class,
            () -> sut.approvePublicationContext(publicationContext.getId(), finalMessage));
    }

    @Test
    @WithMockUser("gestor")
    void approveNonExistingPublicationContext() {
        String finalMessage = "Final message for a publication context.";
        NotFoundException notFoundException = assertThrowsExactly(NotFoundException.class,
            () -> sut.approvePublicationContext("non-existent-id", finalMessage));
        assertTrue(notFoundException.getMessage().contains(PublicationContext.class.getSimpleName()));
    }

    @Test
    @WithMockUser("gestor")
    void approveApprovedPublicationContext() {
        change.addApprovedBy(gestor);
        transactional(() -> em.merge(change, descriptorFactory.changeDescriptor(publicationContext.getUri())));
        em.getEntityManagerFactory().getCache().evictAll();
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.APPROVABLE);
        Comment comment = Generator.generateApprovalCommentOnPC(gestor, publicationContext);
        transactional(() -> em.persist(comment, descriptorFactory.commentDescriptor()));
        em.getEntityManagerFactory().getCache().evictAll();
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.APPROVED);

        String finalMessage = "Final message for a publication context.";
        AlreadyExistsException alreadyExistsException = assertThrowsExactly(AlreadyExistsException.class, () ->
            sut.approvePublicationContext(publicationContext.getId(), finalMessage));
        assertTrue(alreadyExistsException.getMessage().contains("was already reviewed"));
    }

    @Test
    @WithMockUser("gestor")
    void approveRejectedPublicationContext() {
        change.addApprovedBy(gestor);
        transactional(() -> em.merge(change, descriptorFactory.changeDescriptor(publicationContext.getUri())));
        em.getEntityManagerFactory().getCache().evictAll();
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.APPROVABLE);
        Comment comment = Generator.generateRejectionCommentOnPC(gestor, publicationContext);
        transactional(() -> em.persist(comment, descriptorFactory.commentDescriptor()));
        em.getEntityManagerFactory().getCache().evictAll();
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.REJECTED);

        String finalMessage = "Final message for a publication context.";
        AlreadyExistsException alreadyExistsException = assertThrowsExactly(AlreadyExistsException.class, () ->
            sut.approvePublicationContext(publicationContext.getId(), finalMessage));
        assertTrue(alreadyExistsException.getMessage().contains("was already reviewed"));
    }

    @Test
    @WithMockUser("gestor")
    void rejectPublicationContext() {
        String finalMessage = "Final message for a publication context that is long enough.";
        sut.rejectPublicationContext(publicationContext.getId(), finalMessage);
        PublicationContextDetailDto publicationContextDetail =
            sut.getPublicationContextDetail(publicationContext.getId());
        assertEquals(publicationContextDetail.getState(), PublicationContextState.REJECTED);
        CommentDto finalComment = publicationContextDetail.getFinalComment();
        assertNotNull(finalComment);
        assertEquals(finalComment.getAuthor().getId(), gestor.getId());
        assertEquals(finalComment.getTopic(), publicationContext.getUri());
        assertEquals(finalComment.getTag(), CommentTag.REJECTION);
        assertEquals(finalComment.getContent(), finalMessage);
    }

    @Test
    @WithMockUser
    void rejectPublicationContextWithoutPermission() {
        String finalMessage = "Final message for a publication context that is long enough.";
        ForbiddenException forbiddenException = assertThrowsExactly(ForbiddenException.class,
            () -> sut.rejectPublicationContext(publicationContext.getId(), finalMessage));
        assertTrue(forbiddenException.getMessage().contains("can't review publication context"));
    }

    @Test
    @WithMockUser("gestor")
    void rejectNonExistentPublicationContext() {
        String finalMessage = "Final message for a publication context that is long enough.";
        NotFoundException notFoundException = assertThrowsExactly(NotFoundException.class,
            () -> sut.rejectPublicationContext("non-existent-id", finalMessage));
        assertTrue(notFoundException.getMessage().contains(PublicationContext.class.getSimpleName()));
    }

    @Test
    @WithMockUser("gestor")
    void rejectPublicationContextWithShortMessage() {
        String finalMessage = "Too short";
        assertTrue(finalMessage.length() < applicationConfigProperties.getComment().getRejectionMinimalContentLength());
        assertThrowsExactly(RejectionCommentTooShortException.class,
            () -> sut.rejectPublicationContext(publicationContext.getId(), finalMessage));
    }

    @Test
    @WithMockUser("gestor")
    void rejectApprovedPublicationContext() {
        Comment comment = Generator.generateApprovalCommentOnPC(gestor, publicationContext);
        transactional(() -> em.persist(comment, descriptorFactory.commentDescriptor()));
        em.getEntityManagerFactory().getCache().evictAll();
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.APPROVED);

        String finalMessage = "Final message for a publication context.";
        AlreadyExistsException alreadyExistsException = assertThrowsExactly(AlreadyExistsException.class, () ->
            sut.approvePublicationContext(publicationContext.getId(), finalMessage));
        assertTrue(alreadyExistsException.getMessage().contains("was already reviewed"));
    }

    @Test
    @WithMockUser("gestor")
    void rejectRejectedPublicationContext() {
        Comment comment = Generator.generateRejectionCommentOnPC(gestor, publicationContext);
        transactional(() -> em.persist(comment, descriptorFactory.commentDescriptor()));
        em.getEntityManagerFactory().getCache().evictAll();
        assertEquals(sut.getPublicationContextDetail(publicationContext.getId()).getState(),
            PublicationContextState.REJECTED);

        String finalMessage = "Final message for a publication context.";
        AlreadyExistsException alreadyExistsException = assertThrowsExactly(AlreadyExistsException.class, () ->
            sut.approvePublicationContext(publicationContext.getId(), finalMessage));
        assertTrue(alreadyExistsException.getMessage().contains("was already reviewed"));
    }
}