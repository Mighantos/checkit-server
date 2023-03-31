package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class VocabularyDao extends BaseDao<Vocabulary> {

    private final DescriptorFactory descriptorFactory;

    protected VocabularyDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(Vocabulary.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public List<Vocabulary> findAll() {
        try {
            return em.createNativeQuery("SELECT ?voc WHERE { GRAPH ?voc { ?voc a ?type . } }", type)
                .setParameter("type", typeUri)
                .getResultList();
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
    public List<Vocabulary> findAllAffectedVocabularies(URI publicationContextUri) {
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
                .setParameter("type", URI.create(TermVocabulary.s_c_publikacni_kontext))
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("vocType", typeUri)
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<Vocabulary> find(URI uri) {
        Objects.requireNonNull(uri);
        try {
            return Optional.ofNullable(em.find(type, uri, descriptorFactory.vocabularyDescriptor(uri)));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Vocabulary update(Vocabulary entity) {
        Objects.requireNonNull(entity);
        try {
            Vocabulary merged = em.merge(entity, descriptorFactory.vocabularyDescriptor(entity));
            em.getEntityManagerFactory().getCache().evictAll();
            return merged;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts canonical vocabularies.
     *
     * @return number of vocabularies
     */
    public int getAllCount() {
        try {
            return em.createNativeQuery("SELECT (count(?voc) as ?count) WHERE { GRAPH ?voc { ?voc a ?type . } }",
                    Integer.class)
                .setParameter("type", typeUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts canonical vocabularies with at least one gestor.
     *
     * @return number of vocabularies
     */
    public int getGestoredCount() {
        try {
            return em.createNativeQuery("SELECT (count(DISTINCT ?voc) as ?count) WHERE { GRAPH ?voc { "
                    + "?voc a ?type ; "
                    + "     ?gestoredBy ?gestor ."
                    + "} }", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("gestoredBy", URI.create(TermVocabulary.s_p_ma_gestora))
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Find vocabularies gestored by specified user.
     *
     * @param user user
     * @return list of vocabularies
     */
    public List<Vocabulary> findGestoredVocabularies(User user) {
        return em.createNativeQuery("SELECT ?vocab WHERE {"
                + "?vocab ?jeGestorem ?user ."
                + "}", Vocabulary.class)
            .setParameter("user", user.getUri())
            .setParameter("jeGestorem", URI.create(TermVocabulary.s_p_ma_gestora))
            .getResultList();
    }
}
