package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.Notification;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationDao extends BaseDao<Notification> {

    private final DescriptorFactory descriptorFactory;

    protected NotificationDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(Notification.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public List<Notification> findAll() {
        try {
            return em.createNativeQuery("SELECT ?n WHERE { ?n a ?type . }", type)
                .setParameter("type", typeUri)
                .setDescriptor(descriptorFactory.notificationDescriptor())
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds all notifications for specified user.
     *
     * @param userUri    URI identifier of user
     * @param pageNumber page number
     * @param pageSize   page size
     */
    public List<Notification> getAllForUser(URI userUri, int pageNumber, int pageSize) {
        Objects.requireNonNull(userUri);
        try {
            return em.createNativeQuery("SELECT ?n WHERE { "
                    + "?n a ?type ;"
                    + "   ?addressedTo ?user ; "
                    + "   ?created ?time . "
                    + "} ORDER BY DESC(?time)", type)
                .setParameter("type", typeUri)
                .setParameter("addressedTo", URI.create(TermVocabulary.s_p_addressed_to))
                .setParameter("user", userUri)
                .setParameter("created", URI.create(TermVocabulary.s_p_ma_datum_a_cas_vytvoreni))
                .setFirstResult(pageNumber * pageSize)
                .setMaxResults(pageSize)
                .setDescriptor(descriptorFactory.notificationDescriptor())
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<Notification> find(URI id) {
        Objects.requireNonNull(id);
        try {
            return Optional.ofNullable(em.find(type, id, descriptorFactory.notificationDescriptor()));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void persist(Notification entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity, descriptorFactory.notificationDescriptor());
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Notification update(Notification entity) {
        Objects.requireNonNull(entity);
        try {
            return em.merge(entity, descriptorFactory.notificationDescriptor());
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Gets number of unread notifications for current user.
     *
     * @return number of notifications
     */
    public int getUnreadCountForUser(URI userUri) {
        Objects.requireNonNull(userUri);
        try {
            return em.createNativeQuery("SELECT (COUNT(?n) as ?count) WHERE { "
                    + "?n a ?type ;"
                    + "   ?addressedTo ?user . "
                    + "FILTER NOT EXISTS { "
                    + "     ?n ?readAt ?time . "
                    + "     } "
                    + "}", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("addressedTo", URI.create(TermVocabulary.s_p_addressed_to))
                .setParameter("user", userUri)
                .setParameter("readAt", URI.create(TermVocabulary.s_p_read_at))
                .setDescriptor(descriptorFactory.notificationDescriptor())
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
