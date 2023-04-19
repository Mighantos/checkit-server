package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
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
public class PublicationContextDao extends BaseDao<PublicationContext> {

    private final DescriptorFactory descriptorFactory;

    protected PublicationContextDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(PublicationContext.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    /**
     * Finds all open publication contexts.
     *
     * @return list of publication contexts
     */
    public List<PublicationContext> findAllOpen() {
        try {
            return em.createNativeQuery("SELECT ?pc WHERE { "
                    + "?pc a ?type . "
                    + "FILTER NOT EXISTS { "
                    + "     ?comment a ?commentType ; "
                    + "              ?topic ?pc . "
                    + "     } "
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("commentType", URI.create(TermVocabulary.s_c_Comment))
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds all closed publication contexts.
     *
     * @param pageNumber page number
     * @param pageSize   page size
     * @return list of publication contexts
     */
    public List<PublicationContext> findAllClosed(int pageNumber, int pageSize) {
        try {
            return em.createNativeQuery("SELECT ?pc WHERE { "
                    + "?pc a ?type . "
                    + "?comment a ?commentType ; "
                    + "         ?created ?time ; "
                    + "         ?topic ?pc . "
                    + "} ORDER BY DESC(?time)", type)
                .setParameter("type", typeUri)
                .setParameter("commentType", URI.create(TermVocabulary.s_c_Comment))
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .setParameter("created", URI.create(TermVocabulary.s_p_ma_datum_a_cas_vytvoreni))
                .setMaxResults(pageSize)
                .setFirstResult(pageSize * pageNumber)
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds all open publication contexts that has some changes in vocabularies that specified user is gestoring.
     *
     * @param userUri URI identifiers of a user
     * @return list of publication contexts
     */
    public List<PublicationContext> findAllOpenThatAffectVocabulariesGestoredBy(URI userUri) {
        try {
            return em.createNativeQuery("SELECT DISTINCT ?pc WHERE { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc ?gestoredBy ?user . "
                    + "FILTER NOT EXISTS { "
                    + "     ?comment a ?commentType ; "
                    + "              ?topic ?pc . "
                    + "     } "
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("gestoredBy", URI.create(TermVocabulary.s_p_ma_gestora))
                .setParameter("user", userUri)
                .setParameter("commentType", URI.create(TermVocabulary.s_c_Comment))
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
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
     * Checks if not approved publication context related to specified project exists.
     *
     * @param projectContext Project context
     * @return if publication context exists
     */
    public boolean exists(ProjectContext projectContext) {
        try {
            return em.createNativeQuery("ASK { "
                    + "?pc a ?type ; "
                    + "    ?fromProject ?project . "
                    + "FILTER NOT EXISTS { "
                    + "     ?comment a ?commentType ; "
                    + "              ?hasTag ?tag ; "
                    + "              ?topic ?pc . "
                    + "     FILTER(STR(?tag) = ?approval) "
                    + "     } "
                    + "}", Boolean.class)
                .setParameter("type", typeUri)
                .setParameter("fromProject", URI.create(TermVocabulary.s_p_z_projektu))
                .setParameter("project", projectContext.getUri())
                .setParameter("commentType", URI.create(TermVocabulary.s_c_Comment))
                .setParameter("hasTag", URI.create(TermVocabulary.s_p_ma_stitek))
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .setParameter("approval", CommentTag.APPROVAL)
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
     * Finds not approved publication context related to specified project.
     *
     * @param projectContext project context
     * @return publication context
     */
    public Optional<PublicationContext> findByProject(ProjectContext projectContext) {
        try {
            URI uri = em.createNativeQuery("SELECT DISTINCT ?pc WHERE { "
                    + "?pc a ?type ; "
                    + "    ?fromProject ?project . "
                    + "FILTER NOT EXISTS { "
                    + "     ?comment a ?commentType ; "
                    + "              ?hasTag ?tag ; "
                    + "              ?topic ?pc . "
                    + "     FILTER(STR(?tag) = ?approval) "
                    + "     } "
                    + "}", URI.class)
                .setParameter("type", typeUri)
                .setParameter("fromProject", URI.create(TermVocabulary.s_p_z_projektu))
                .setParameter("project", projectContext.getUri())
                .setParameter("commentType", URI.create(TermVocabulary.s_c_Comment))
                .setParameter("hasTag", URI.create(TermVocabulary.s_p_ma_stitek))
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .setParameter("approval", CommentTag.APPROVAL)
                .getSingleResult();
            return find(uri);
        } catch (NoResultException nre) {
            return Optional.empty();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Checks if specified publication context has some change that is in vocabulary gestored by specified user.
     *
     * @param publicationContextUri URI identifier of publication context
     * @param userUri               URI identifier of user
     * @return true or false
     */
    public boolean canUserReview(URI publicationContextUri, URI userUri) {
        try {
            return em.createNativeQuery("ASK { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc ?gestoredBy ?user . "
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
            return em.createNativeQuery("ASK { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc ?gestoredBy ?user . "
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

    /**
     * Counts closed publication contexts.
     *
     * @return number of publication contexts
     */
    public Integer countAllClosed() {
        try {
            return em.createNativeQuery("SELECT (COUNT(DISTINCT ?pc) as ?count) WHERE { "
                    + "?pc a ?type . "
                    + "?comment a ?commentType ; "
                    + "         ?topic ?pc . "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("commentType", URI.create(TermVocabulary.s_c_Comment))
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts changes in specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @return number of changes
     */
    public Integer countChanges(URI publicationContextUri) {
        Objects.requireNonNull(publicationContextUri);
        try {
            return em.createNativeQuery("SELECT (COUNT(DISTINCT ?change) as ?count) WHERE { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("pc", publicationContextUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setDescriptor(descriptorFactory.publicationContextDescriptor(publicationContextUri))
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts changes in specified publication context in specified vocabulary.
     *
     * @param publicationContextUri URI identifier of publication context
     * @param vocabularyUri         URI identifier of vocabulary
     * @return number of changes
     */
    public Integer countChangesInVocabulary(URI publicationContextUri, URI vocabularyUri) {
        Objects.requireNonNull(publicationContextUri);
        Objects.requireNonNull(vocabularyUri);
        try {
            return em.createNativeQuery("SELECT (COUNT(DISTINCT ?change) as ?count) WHERE { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("pc", publicationContextUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("voc", vocabularyUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts changes reviewable by specified user in specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @param userUri               URI identifier of user
     * @return number of changes
     */
    public Integer countReviewableChanges(URI publicationContextUri, URI userUri) {
        Objects.requireNonNull(publicationContextUri);
        Objects.requireNonNull(userUri);
        try {
            return em.createNativeQuery("SELECT (COUNT(DISTINCT ?change) as ?count) WHERE { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc ?gestoredBy ?user . "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("pc", publicationContextUri)
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
     * Counts approved changes in specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @return number of changes
     */
    public Integer countApprovedChanges(URI publicationContextUri) {
        Objects.requireNonNull(publicationContextUri);
        try {
            return em.createNativeQuery("SELECT (COUNT(DISTINCT ?change) as ?count) WHERE { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?approvedBy ?user . "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("pc", publicationContextUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("approvedBy", URI.create(TermVocabulary.s_p_schvaleno))
                .setDescriptor(descriptorFactory.publicationContextDescriptor(publicationContextUri))
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts changes approved by specified user in specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @param userUri               URI identifier of user
     * @return number of changes
     */
    public Integer countApprovedChanges(URI publicationContextUri, URI userUri) {
        Objects.requireNonNull(publicationContextUri);
        Objects.requireNonNull(userUri);
        try {
            return em.createNativeQuery("SELECT (COUNT(DISTINCT ?change) as ?count) WHERE { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?approvedBy ?user . "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("pc", publicationContextUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("approvedBy", URI.create(TermVocabulary.s_p_schvaleno))
                .setParameter("user", userUri)
                .setDescriptor(descriptorFactory.publicationContextDescriptor(publicationContextUri))
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts changes approved by specified user in specified publication context in specified vocabulary.
     *
     * @param publicationContextUri URI identifier of publication context
     * @param userUri               URI identifier of user
     * @param vocabularyUri         URI identifier of vocabulary
     * @return number of changes
     */
    public Integer countApprovedChangesInVocabulary(URI publicationContextUri, URI userUri, URI vocabularyUri) {
        Objects.requireNonNull(publicationContextUri);
        Objects.requireNonNull(userUri);
        Objects.requireNonNull(vocabularyUri);
        try {
            return em.createNativeQuery("SELECT (COUNT(DISTINCT ?change) as ?count) WHERE { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?approvedBy ?user ; "
                    + "        ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("pc", publicationContextUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("approvedBy", URI.create(TermVocabulary.s_p_schvaleno))
                .setParameter("user", userUri)
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("voc", vocabularyUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts changes rejected by specified user in specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @param userUri               URI identifier of user
     * @return number of changes
     */
    public Integer countRejectedChanges(URI publicationContextUri, URI userUri) {
        Objects.requireNonNull(publicationContextUri);
        Objects.requireNonNull(userUri);
        try {
            return em.createNativeQuery("SELECT (COUNT(DISTINCT ?change) as ?count) WHERE { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?rejectedBy ?user . "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("pc", publicationContextUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("rejectedBy", URI.create(TermVocabulary.s_p_zamitnuto))
                .setParameter("user", userUri)
                .setDescriptor(descriptorFactory.publicationContextDescriptor(publicationContextUri))
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts changes rejected by specified user in specified publication context in specified vocabulary.
     *
     * @param publicationContextUri URI identifier of publication context
     * @param userUri               URI identifier of user
     * @param vocabularyUri         URI identifier of vocabulary
     * @return number of changes
     */
    public Integer countRejectedChangesInVocabulary(URI publicationContextUri, URI userUri, URI vocabularyUri) {
        Objects.requireNonNull(publicationContextUri);
        Objects.requireNonNull(userUri);
        Objects.requireNonNull(vocabularyUri);
        try {
            return em.createNativeQuery("SELECT (COUNT(DISTINCT ?change) as ?count) WHERE { "
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?rejectedBy ?user ; "
                    + "        ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("pc", publicationContextUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("rejectedBy", URI.create(TermVocabulary.s_p_zamitnuto))
                .setParameter("user", userUri)
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("voc", vocabularyUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
