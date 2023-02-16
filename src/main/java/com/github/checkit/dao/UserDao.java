package com.github.checkit.dao;

import com.github.checkit.model.User;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends BaseDao<User> {
    protected UserDao(EntityManager em) {
        super(User.class, em);
    }
}
