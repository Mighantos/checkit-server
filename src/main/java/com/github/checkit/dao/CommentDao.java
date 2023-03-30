package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.Comment;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
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
                    + "         ?topic ?change . "
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .setParameter("change", changeUri)
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<Comment> find(URI id) {
        Objects.requireNonNull(id);
        try {
            Descriptor descriptor = descriptorFactory.commentDescriptor();
            return Optional.ofNullable(em.find(type, id, descriptor));
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
}
