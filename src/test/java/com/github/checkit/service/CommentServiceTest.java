package com.github.checkit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dto.CommentDto;
import com.github.checkit.environment.Generator;
import com.github.checkit.exception.ChangeNotRejectedException;
import com.github.checkit.exception.ForbiddenException;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.model.Change;
import com.github.checkit.model.Comment;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.model.auxilary.CommentTag;
import com.github.checkit.service.auxilary.BaseServiceTestRunner;
import com.github.checkit.util.TermVocabulary;
import java.net.URI;
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
class CommentServiceTest extends BaseServiceTestRunner {

    @Autowired
    private CommentService sut;

    @Autowired
    private RepositoryConfigProperties repositoryConfigProperties;

    private User user;
    private User gestor;
    private Vocabulary vocabularyWithGestor;
    private VocabularyContext vocabularyContextWithGestor;
    private Change changeWithGestor;
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
        this.projectContext =
            Generator.generateProjectContext(user, Collections.singleton(vocabularyContextWithGestor));
        Set<Change> changes = new HashSet<>();
        this.changeWithGestor = Generator.generateCreateChange(vocabularyContextWithGestor);
        changes.add(changeWithGestor);
        this.publicationContext = Generator.generatePublicationContext(projectContext, changes);
        transactional(() -> {
            em.persist(vocabularyWithGestor, descriptorFactory.vocabularyDescriptor(vocabularyWithGestor));
            em.persist(vocabularyContextWithGestor,
                descriptorFactory.vocabularyDescriptor(vocabularyContextWithGestor.getUri()));
            em.persist(projectContext, descriptorFactory.projectContextDescriptor(projectContext));
            em.persist(publicationContext, descriptorFactory.publicationContextDescriptor(publicationContext));
        });
    }

    @Test
    @WithMockUser
    void findAllInDiscussionRelatedToChange() {
        List<CommentDto> discussionComments = sut.findAllInDiscussionRelatedToChange(changeWithGestor.getUri());
        assertEquals(discussionComments.size(), 0);

        transactional(() -> em.persist(Generator.generateDiscussionComment(gestor, changeWithGestor),
            descriptorFactory.commentDescriptor()));
        discussionComments = sut.findAllInDiscussionRelatedToChange(changeWithGestor.getUri());
        assertEquals(discussionComments.size(), 1);

        transactional(() -> em.persist(Generator.generateRejectionCommentOnChange(gestor, changeWithGestor),
            descriptorFactory.commentDescriptor()));
        discussionComments = sut.findAllInDiscussionRelatedToChange(changeWithGestor.getUri());
        assertEquals(discussionComments.size(), 1);
    }

    @Test
    @WithMockUser
    void getDiscussionCommentsCount() {
        int discussionCommentsCount = sut.getDiscussionCommentsCount(changeWithGestor);
        assertEquals(discussionCommentsCount, 0);

        transactional(() -> em.persist(Generator.generateDiscussionComment(gestor, changeWithGestor),
            descriptorFactory.commentDescriptor()));
        discussionCommentsCount = sut.getDiscussionCommentsCount(changeWithGestor);
        assertEquals(discussionCommentsCount, 1);

        transactional(() -> em.persist(Generator.generateRejectionCommentOnChange(gestor, changeWithGestor),
            descriptorFactory.commentDescriptor()));
        discussionCommentsCount = sut.getDiscussionCommentsCount(changeWithGestor);
        assertEquals(discussionCommentsCount, 1);
    }

    @Test
    @WithMockUser
    void findFinalComment() {
    }

    @Test
    @WithMockUser
    void createDiscussionComment() {
        String commentContent = "Test comment";
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());

        URI testCommentUri = sut.createDiscussionComment(changeWithGestor.getUri(), commentContent);
        assertTrue(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());
        Comment testComment = em.find(Comment.class, testCommentUri, descriptorFactory.commentDescriptor());
        assertEquals(testComment.getTag(), CommentTag.DISCUSSION);
        assertEquals(testComment.getAuthor(), user);
        assertEquals(testComment.getTopic().getUri(), changeWithGestor.getUri());
        assertEquals(testComment.getContent(), commentContent);
    }

    @Test
    @WithMockUser
    void createDiscussionCommentOnNonExistingChange() {
        String commentContent = "Test comment";
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());

        NotFoundException notFoundException = assertThrowsExactly(NotFoundException.class,
            () -> sut.createDiscussionComment(URI.create("http://example.com/non/existing"),
                commentContent));
        assertTrue(notFoundException.getMessage().contains("Change identified by"));
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());
    }

    @Test
    @WithMockUser("gestor")
    void createRejectionComment() {
        String commentContent = "Test rejection comment";
        changeWithGestor.addRejectedBy(gestor);
        transactional(
            () -> em.merge(changeWithGestor, descriptorFactory.changeDescriptor(publicationContext.getUri())));
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());

        URI testCommentUri = sut.createRejectionComment(changeWithGestor.getUri(), commentContent);
        assertTrue(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());
        Comment testComment = em.find(Comment.class, testCommentUri, descriptorFactory.commentDescriptor());
        assertEquals(testComment.getTag(), CommentTag.REJECTION);
        assertEquals(testComment.getAuthor(), gestor);
        assertEquals(testComment.getTopic().getUri(), changeWithGestor.getUri());
        assertEquals(testComment.getContent(), commentContent);
    }

    @Test
    @WithMockUser
    void createRejectionCommentWithNonGestorUser() {
        String commentContent = "Test rejection comment";
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());

        ForbiddenException forbiddenException =
            assertThrowsExactly(ForbiddenException.class, () -> sut.createRejectionComment(changeWithGestor.getUri(),
                commentContent));
        assertTrue(forbiddenException.getMessage().contains("can't review change"));
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());
    }

    @Test
    @WithMockUser("gestor")
    void createRejectionCommentWithNotRejectedChange() {
        String commentContent = "Test rejection comment";
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());

        assertThrowsExactly(ChangeNotRejectedException.class,
            () -> sut.createRejectionComment(changeWithGestor.getUri(), commentContent));
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());
    }

    @Test
    @WithMockUser
    void createRejectionCommentOnNonExistingChange() {
        String commentContent = "Test comment";
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());

        NotFoundException notFoundException = assertThrowsExactly(NotFoundException.class,
            () -> sut.createRejectionComment(URI.create("http://example.com/non/existing"),
                commentContent));
        assertTrue(notFoundException.getMessage().contains("Change identified by"));
        assertFalse(em.createNativeQuery("ASK {?s a ?o . }", Boolean.class)
            .setParameter("o", URI.create(TermVocabulary.s_c_Comment)).getSingleResult());
    }
}