package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PublicationContextDao extends BaseDao<PublicationContext> {

    private final DescriptorFactory descriptorFactory;

    protected PublicationContextDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(PublicationContext.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public void persist(PublicationContext entity) {
        Objects.requireNonNull(entity);
        try {
            entity.setUri(generateEntityUri());
            Descriptor descriptor = descriptorFactory.publicationContextDescriptor(entity);
            em.persist(entity, descriptor);
            em.getEntityManagerFactory().getCache().evict(PublicationContext.class);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public PublicationContext update(PublicationContext entity) {
        Objects.requireNonNull(entity);
        try {
            Descriptor descriptor = descriptorFactory.publicationContextDescriptor(entity);
            PublicationContext merged = em.merge(entity, descriptor);
            em.getEntityManagerFactory().getCache().evict(PublicationContext.class);
            return merged;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Returns if publication context related to specified project exists.
     *
     * @param projectContext Project context
     * @return if publication context exists
     */
    public boolean exists(ProjectContext projectContext) {
        try {
            return em.createNativeQuery("ASK { ?pc a ?type ; "
                    + "?fromProject ?project . "
                    + "}", Boolean.class)
                .setParameter("type", typeUri)
                .setParameter("fromProject", URI.create(TermVocabulary.s_p_z_projektu))
                .setParameter("project", projectContext.getUri())
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<PublicationContext> find(URI id) {
        Objects.requireNonNull(id);
        try {
            Descriptor descriptor = descriptorFactory.publicationContextDescriptor(id);
            return Optional.ofNullable(em.find(type, id, descriptor));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds publication context related to specified project.
     *
     * @param projectContext project context
     * @return URI identifier publication context
     */
    public Optional<URI> find(ProjectContext projectContext) {
        try {
            return Optional.ofNullable(em.createNativeQuery("SELECT ?pc WHERE { ?pc a ?type ; "
                    + "?fromProject ?project . "
                    + "}", URI.class)
                .setParameter("type", typeUri)
                .setParameter("fromProject", URI.create(TermVocabulary.s_p_z_projektu))
                .setParameter("project", projectContext.getUri())
                .getSingleResult());
        } catch (NoResultException nre) {
            return Optional.empty();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
