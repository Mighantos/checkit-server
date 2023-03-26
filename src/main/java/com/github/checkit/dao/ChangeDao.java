package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.Change;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ChangeDao extends BaseDao<Change> {

    private final DescriptorFactory descriptorFactory;

    protected ChangeDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(Change.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public Optional<Change> find(URI uri) {
        Objects.requireNonNull(uri);
        try {
            Descriptor descriptor = descriptorFactory.changeDescriptor(resolvePublicationContext(uri));
            return Optional.ofNullable(em.find(type, uri, descriptor));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Change update(Change entity) {
        Objects.requireNonNull(entity);
        try {
            Descriptor descriptor = descriptorFactory.changeDescriptor(resolvePublicationContext(entity.getUri()));
            Change merged = em.merge(entity, descriptor);
            em.getEntityManagerFactory().getCache().evict(Change.class);
            return merged;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Checks if specified user is gestoring vocabulary in which specified change is made.
     *
     * @param userUri   URI identifier of user
     * @param changeUri URI identifier of change
     * @return true of false
     */
    public boolean isUserGestorOfVocabularyWithChange(URI userUri, URI changeUri) {
        try {
            return em.createNativeQuery("ASK {"
                    + "?change a ?type ; "
                    + "    ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc ?gestoredBy ?user ."
                    + "}", Boolean.class)
                .setParameter("change", changeUri)
                .setParameter("type", typeUri)
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("gestoredBy", URI.create(TermVocabulary.s_p_ma_gestora))
                .setParameter("user", userUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    private URI resolvePublicationContext(URI changeUri) {
        try {
            return em.createNativeQuery("SELECT ?pc WHERE {"
                    + "?change a ?type . "
                    + "?pc ?hasChange ?change ."
                    + "}", URI.class)
                .setParameter("change", changeUri)
                .setParameter("type", typeUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
