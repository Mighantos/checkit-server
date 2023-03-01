package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.persistence.DescriptorFactory;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class VocabularyDao extends BaseDao<Vocabulary> {

    private final DescriptorFactory descriptorFactory;

    protected VocabularyDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(Vocabulary.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public Vocabulary update(Vocabulary entity) {
        Objects.requireNonNull(entity);
        try {
            return em.merge(entity, descriptorFactory.vocabularyDescriptor(entity));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
