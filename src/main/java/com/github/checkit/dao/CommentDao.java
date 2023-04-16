package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.Change;
import com.github.checkit.model.Comment;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.auxilary.CommentTag;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CommentDao extends BaseDao<Comment> {

    private final DescriptorFactory descriptorFactory;

    protected CommentDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(Comment.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    /**
     * Finds all comments related to specified change.
     *
     * @param changeUri URI identifier of change
     * @return list of comments
     */
    public List<Comment> findAllRelatedToChange(URI changeUri) {
        try {
            return em.createNativeQuery("SELECT ?comment WHERE { "
                    + "?comment a ?type ; "
                    + "         ?topic ?change ; "
                    + "         ?hasTag ?tag . "
                    + "FILTER(STR(?tag) = ?discussion) "
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .setParameter("change", changeUri)
                .setParameter("hasTag", URI.create(TermVocabulary.s_p_ma_stitek))
                .setParameter("discussion", CommentTag.DISCUSSION)
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds all final comments on specified change.
     *
     * @param changeUri URI identifier of change
     * @return list of final comments
     */
    public List<Comment> findAllFinalComments(URI changeUri) {
        Objects.requireNonNull(changeUri);
        try {
            Descriptor descriptor = descriptorFactory.commentDescriptor();
            return em.createNativeQuery("SELECT ?comment WHERE { "
                    + "?comment a ?type ; "
                    + "         ?topic ?change ; "
                    + "         ?hasTag ?tag . "
                    + "FILTER(STR(?tag) != ?discussion) "
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .setParameter("change", changeUri)
                .setParameter("hasTag", URI.create(TermVocabulary.s_p_ma_stitek))
                .setParameter("discussion", CommentTag.DISCUSSION)
                .setDescriptor(descriptor)
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<Comment> find(URI uri) {
        Objects.requireNonNull(uri);
        try {
            Descriptor descriptor = descriptorFactory.commentDescriptor();
            return Optional.ofNullable(em.find(type, uri, descriptor));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds final comment on specified publication context.
     *
     * @param publicationContext publication context
     * @return Comment (if exists)
     */
    public Optional<Comment> findFinalComment(PublicationContext publicationContext) {
        URI publicationContextUri = publicationContext.getUri();
        Objects.requireNonNull(publicationContextUri);
        try {
            Descriptor descriptor = descriptorFactory.commentDescriptor();
            return Optional.ofNullable(em.createNativeQuery("SELECT ?comment WHERE { "
                    + "?comment a ?type ; "
                    + "         ?topic ?pc . "
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .setParameter("pc", publicationContextUri)
                .setDescriptor(descriptor)
                .getSingleResult());
        } catch (NoResultException nre) {
            return Optional.empty();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds final comment on specified change created by specified user.
     *
     * @param change change
     * @param user   user
     * @return Comment (if exists)
     */
    public Optional<Comment> findFinalComment(Change change, User user) {
        URI changeUri = change.getUri();
        URI userUri = user.getUri();
        Objects.requireNonNull(changeUri);
        Objects.requireNonNull(userUri);
        try {
            Descriptor descriptor = descriptorFactory.commentDescriptor();
            return Optional.ofNullable(em.createNativeQuery("SELECT ?comment WHERE { "
                    + "?comment a ?type ; "
                    + "         ?topic ?change ; "
                    + "         ?hasCreator ?user ; "
                    + "         ?hasTag ?tag . "
                    + "FILTER(STR(?tag) != ?discussion) "
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .setParameter("change", changeUri)
                .setParameter("hasCreator", URI.create(TermVocabulary.s_p_has_creator))
                .setParameter("user", userUri)
                .setParameter("hasTag", URI.create(TermVocabulary.s_p_ma_stitek))
                .setParameter("discussion", CommentTag.DISCUSSION)
                .setDescriptor(descriptor)
                .getSingleResult());
        } catch (NoResultException nre) {
            return Optional.empty();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void persist(Comment entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity, descriptorFactory.commentDescriptor());
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Comment update(Comment entity) {
        Objects.requireNonNull(entity);
        try {
            return em.merge(entity, descriptorFactory.commentDescriptor());
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(Comment entity) {
        Objects.requireNonNull(entity);
        try {
            em.remove(em.merge(entity, descriptorFactory.commentDescriptor()));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
