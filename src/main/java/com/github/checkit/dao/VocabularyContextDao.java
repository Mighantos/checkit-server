package com.github.checkit.dao;

import com.github.checkit.model.VocabularyContext;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class VocabularyContextDao extends BaseDao<VocabularyContext> {

    protected VocabularyContextDao(EntityManager em) {
        super(VocabularyContext.class, em);
    }
}
