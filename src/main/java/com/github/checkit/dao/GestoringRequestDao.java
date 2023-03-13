package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.GestoringRequest;
import com.github.checkit.persistence.DescriptorFactory;
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

    @Override
    public List<GestoringRequest> findAll() {
        try {
            return em.createNativeQuery("SELECT ?x WHERE { ?x a ?type . }", type)
                .setParameter("type", typeUri)
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
}
