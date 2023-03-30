package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.Vocabulary;
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
public class PublicationContextDao extends BaseDao<PublicationContext> {

    private final DescriptorFactory descriptorFactory;

    protected PublicationContextDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(PublicationContext.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    /**
     * Finds all publication contexts that has some changes in vocabularies that specified user is gestoring.
     *
     * @param userUri URI identifiers of a user
     * @return list of publication contexts
     */
    public List<PublicationContext> findAllThatAffectVocabulariesGestoredBy(URI userUri) {
        try {
            return em.createNativeQuery("SELECT DISTINCT ?pc WHERE {"
                    + "?pc a ?type ;"
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc ?gestoredBy ?user ."
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("gestoredBy", URI.create(TermVocabulary.s_p_ma_gestora))
                .setParameter("user", userUri)
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
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
     * Checks if publication context related to specified project exists.
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
    public Optional<PublicationContext> find(URI uri) {
        Objects.requireNonNull(uri);
        try {
            Descriptor descriptor = descriptorFactory.publicationContextDescriptor(uri);
            return Optional.ofNullable(em.find(type, uri, descriptor));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds publication context related to specified project.
     *
     * @param projectContext project context
     * @return publication context
     */
    public Optional<PublicationContext> findByProject(ProjectContext projectContext) {
        try {
            URI uri = em.createNativeQuery("SELECT DISTINCT ?pc WHERE { ?pc a ?type ; "
                    + "?fromProject ?project . "
                    + "}", URI.class)
                .setParameter("type", typeUri)
                .setParameter("fromProject", URI.create(TermVocabulary.s_p_z_projektu))
                .setParameter("project", projectContext.getUri())
                .getSingleResult();
            return find(uri);
        } catch (NoResultException nre) {
            return Optional.empty();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds affected vocabularies of specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @return list of canonical vocabularies
     */
    public List<Vocabulary> findAffectedVocabularies(URI publicationContextUri) {
        Objects.requireNonNull(publicationContextUri);
        try {
            return em.createNativeQuery("SELECT DISTINCT ?voc WHERE {"
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc a ?vocType . "
                    + "}", Vocabulary.class)
                .setParameter("pc", publicationContextUri)
                .setParameter("type", typeUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("vocType", URI.create(TermVocabulary.s_c_slovnik))
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Checks if specified publication context has some change that is in vocabulary gestored by specified user.
     *
     * @param userUri               URI identifier of user
     * @param publicationContextUri URI identifier of publication context
     * @return true or false
     */
    public boolean doesUserHaveAnyChangesToReview(URI userUri, URI publicationContextUri) {
        try {
            return em.createNativeQuery("ASK {"
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc ?gestoredBy ?user ."
                    + "}", Boolean.class)
                .setParameter("pc", publicationContextUri)
                .setParameter("type", typeUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("gestoredBy", URI.create(TermVocabulary.s_p_ma_gestora))
                .setParameter("user", userUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Checks if specified user is gestor of specified vocabulary present in specified publication context.
     *
     * @param userUri               URI identifier of user
     * @param publicationContextUri URI identifier of publication context
     * @param vocabularyUri         URI identifier of vocabulary
     * @return true or false
     */
    public boolean doesUserHavePermissionToReviewVocabulary(URI userUri, URI publicationContextUri, URI vocabularyUri) {
        try {
            return em.createNativeQuery("ASK {"
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc ?gestoredBy ?user ."
                    + "}", Boolean.class)
                .setParameter("pc", publicationContextUri)
                .setParameter("type", typeUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("voc", vocabularyUri)
                .setParameter("gestoredBy", URI.create(TermVocabulary.s_p_ma_gestora))
                .setParameter("user", userUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
