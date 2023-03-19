package com.github.checkit.dao;

import com.github.checkit.model.Change;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class ChangeDao extends BaseDao<Change> {

    protected ChangeDao(EntityManager em) {
        super(Change.class, em);
    }
}
