package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.GestoringRequest;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class GestoringRequestDao extends BaseDao<GestoringRequest> {

    private final DescriptorFactory descriptorFactory;

    protected GestoringRequestDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(GestoringRequest.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    /**
     * Find all gestoring requests with specified user as an applicant.
     *
     * @param applicantUri user URI
     * @return list of gestoring requests
     */
    public List<GestoringRequest> findAllFromApplicant(URI applicantUri) {
        try {
            return em.createNativeQuery("SELECT ?x WHERE { ?x a ?type ; "
                    + "?applies ?applicant . "
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("applies", URI.create(TermVocabulary.s_p_ma_zadatele))
                .setParameter("applicant", applicantUri)
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<GestoringRequest> find(URI uri) {
        Objects.requireNonNull(uri);
        try {
            return Optional.ofNullable(em.find(type, uri, descriptorFactory.gestoringRequestDescriptor()));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds gestoring request with specified user and vocabulary.
     *
     * @param applicantUri  User URI of applicant
     * @param vocabularyUri Vocabulary URI applicant wants to gestor
     * @return {@link GestoringRequest}
     */
    public Optional<GestoringRequest> find(URI vocabularyUri, URI applicantUri) {
        Objects.requireNonNull(applicantUri);
        Objects.requireNonNull(vocabularyUri);
        try {
            return Optional.ofNullable(em.createNativeQuery("SELECT ?gr { ?gr a ?type ; "
                    + "?applies ?applicant ; "
                    + "?requests ?vocabulary . "
                    + "}", GestoringRequest.class)
                .setParameter("type", typeUri)
                .setParameter("applies", URI.create(TermVocabulary.s_p_ma_zadatele))
                .setParameter("applicant", applicantUri)
                .setParameter("requests", URI.create(TermVocabulary.s_p_zada_o_gestorovani))
                .setParameter("vocabulary", vocabularyUri)
                .getSingleResult());
        } catch (NoResultException nre) {
            return Optional.empty();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void persist(GestoringRequest entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity, descriptorFactory.gestoringRequestDescriptor());
            em.getEntityManagerFactory().getCache().evict(GestoringRequest.class);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(GestoringRequest entity) {
        Objects.requireNonNull(entity);
        try {
            em.remove(entity);
            em.getEntityManagerFactory().getCache().evict(GestoringRequest.class);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts gestoring requests.
     *
     * @return number of gestoring requests
     */
    public int getAllCount() {
        try {
            return em.createNativeQuery("SELECT (count(?gr) as ?count) WHERE { ?gr a ?type . }", Integer.class)
                .setParameter("type", typeUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds if gestoring request with specified user and vocabulary exists.
     *
     * @param applicantUri  User URI of applicant
     * @param vocabularyUri Vocabulary URI applicant wants to gestor
     * @return if gestoring request exists
     */
    public boolean exists(URI applicantUri, URI vocabularyUri) {
        Objects.requireNonNull(applicantUri);
        Objects.requireNonNull(vocabularyUri);
        try {
            return em.createNativeQuery("ASK { ?x a ?type ; "
                    + "?applies ?applicant ; "
                    + "?requests ?vocabulary . "
                    + "}", Boolean.class)
                .setParameter("type", typeUri)
                .setParameter("applies", URI.create(TermVocabulary.s_p_ma_zadatele))
                .setParameter("applicant", applicantUri)
                .setParameter("requests", URI.create(TermVocabulary.s_p_zada_o_gestorovani))
                .setParameter("vocabulary", vocabularyUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
