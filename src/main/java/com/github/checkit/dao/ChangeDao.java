package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeType;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import java.net.URI;
import java.time.Instant;
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
            Descriptor descriptor = descriptorFactory.changeDescriptor(resolvePublicationContextUri(uri));
            return Optional.ofNullable(em.find(type, uri, descriptor));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds any change in specified context of specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @param contextUri            URI identifier of context
     * @return change if context is present publication context
     */
    public Optional<Change> findAnyInContextInPublicationContext(URI publicationContextUri, URI contextUri) {
        Objects.requireNonNull(publicationContextUri);
        Objects.requireNonNull(contextUri);
        try {
            Optional<URI> anyChange = em.createNativeQuery("SELECT ?change WHERE { "
                    + "?change a ?type ; "
                    + "        ?inContext ?ctx ; "
                    + "        ?isOfType ?changeType . "
                    + "FILTER (STR(?changeType) != ?rollbacked) "
                    + "?pc ?hasChange ?change . "
                    + "}", URI.class)
                .setParameter("type", typeUri)
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("ctx", contextUri)
                .setParameter("pc", publicationContextUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("isOfType", URI.create(TermVocabulary.s_p_je_typu))
                .setParameter("rollbacked", URI.create(TermVocabulary.s_c_vraceno_zpet))
                .getResultStream().findFirst();
            if (anyChange.isEmpty()) {
                return Optional.empty();
            }
            return find(anyChange.get());
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Change update(Change entity) {
        Objects.requireNonNull(entity);
        try {
            Descriptor descriptor = descriptorFactory.changeDescriptor(resolvePublicationContextUri(entity.getUri()));
            Change merged = em.merge(entity, descriptor);
            em.getEntityManagerFactory().getCache().evict(entity.getUri());
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
        Objects.requireNonNull(userUri);
        Objects.requireNonNull(changeUri);
        try {
            return em.createNativeQuery("ASK { "
                    + "?change a ?type ; "
                    + "        ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "?voc ?gestoredBy ?user . "
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

    /**
     * Checks if publication context of specified change is not closed.
     *
     * @param changeUri URI identifier of change
     * @return true of false
     */
    public boolean isChangesPublicationContextClosed(URI changeUri) {
        Objects.requireNonNull(changeUri);
        try {
            return em.createNativeQuery("ASK { "
                    + "?change a ?type . "
                    + "?pc ?hasChange ?change . "
                    + "?comment ?hasTopic ?pc . "
                    + "}", Boolean.class)
                .setParameter("change", changeUri)
                .setParameter("type", typeUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("hasTopic", URI.create(TermVocabulary.s_p_topic))
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Checks if publication context of specified change was updated.
     *
     * @param changeUri   URI identifier of change
     * @param versionDate date of publication context last update
     * @return true of false
     */
    public boolean wasChangesPublicationContextUpdated(URI changeUri, Instant versionDate) {
        Objects.requireNonNull(changeUri);
        try {
            return !em.createNativeQuery("ASK { "
                    + "?change a ?type . "
                    + "?pc ?hasChange ?change ; "
                    + "    ?updated ?time . "
                    + "}", Boolean.class)
                .setParameter("change", changeUri)
                .setParameter("type", typeUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("updated", URI.create(TermVocabulary.s_p_ma_datum_a_cas_posledni_modifikace))
                .setParameter("time", versionDate)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    private URI resolvePublicationContextUri(URI changeUri) {
        Objects.requireNonNull(changeUri);
        try {
            return em.createNativeQuery("SELECT ?pc WHERE { "
                    + "?change a ?type . "
                    + "?pc ?hasChange ?change . "
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
