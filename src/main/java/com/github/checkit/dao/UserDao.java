package com.github.checkit.dao;

import com.github.checkit.model.User;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends BaseDao<User> {
    protected UserDao(EntityManager em) {
        super(User.class, em);
    }

    private Set<URI> loadGestoredVocabularies(User user) {
        return new HashSet<>(em.createNativeQuery("SELECT ?vocab WHERE {"
                + "?user ?jeGestorem ?vocab ."
                + "}", URI.class)
            .setParameter("user", user.getUri())
            .setParameter("jeGestorem", URI.create(TermVocabulary.s_p_je_gestorem))
            .getResultList());
    }
}
