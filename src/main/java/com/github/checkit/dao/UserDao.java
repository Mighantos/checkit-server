package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.User;
import com.github.checkit.model.auxilary.CommentTag;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends BaseDao<User> {
    protected UserDao(EntityManager em) {
        super(User.class, em);
    }

    /**
     * Finds all users in discussion comments related to specified change.
     *
     * @param changeUri URI identifier of change
     * @return set of users
     */
    public Set<User> findAllInDiscussionOnChange(URI changeUri) {
        Objects.requireNonNull(changeUri);
        try {
            return new HashSet<>(em.createNativeQuery("SELECT DISTINCT ?user WHERE { "
                    + "?user a ?type . "
                    + "?comment ?topic ?change ; "
                    + "         ?hasAuthor ?user ; "
                    + "         ?hasTag ?tag . "
                    + "FILTER(STR(?tag) = ?discussion) "
                    + "}", type)
                .setParameter("type", typeUri)
                .setParameter("topic", URI.create(TermVocabulary.s_p_topic))
                .setParameter("change", changeUri)
                .setParameter("hasAuthor", URI.create(TermVocabulary.s_p_has_creator))
                .setParameter("hasTag", URI.create(TermVocabulary.s_p_ma_stitek))
                .setParameter("discussion", CommentTag.DISCUSSION)
                .getResultList());
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
