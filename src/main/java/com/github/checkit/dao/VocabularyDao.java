package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.persistence.DescriptorFactory;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
            em.getEntityManagerFactory().getCache().evict(Vocabulary.class);
            return merged;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
