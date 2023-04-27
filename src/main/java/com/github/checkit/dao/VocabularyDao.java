package com.github.checkit.dao;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Repository;

@Repository
public class VocabularyDao extends BaseDao<Vocabulary> {

    private final DescriptorFactory descriptorFactory;
    private final RepositoryConfigProperties repositoryConfigProperties;

    protected VocabularyDao(EntityManager em, DescriptorFactory descriptorFactory,
                            RepositoryConfigProperties repositoryConfigProperties) {
        super(Vocabulary.class, em);
        this.descriptorFactory = descriptorFactory;
        this.repositoryConfigProperties = repositoryConfigProperties;
    }

    @Override
    public List<Vocabulary> findAll() {
        try {
            return em.createNativeQuery("SELECT ?voc WHERE { GRAPH ?voc { ?voc a ?type . } }", URI.class)
                .setParameter("type", typeUri)
                .getResultStream().map(this::find).flatMap(Optional::stream).collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Find vocabularies gestored by specified user.
     *
     * @param user user
     * @return list of vocabularies
     */
    public List<Vocabulary> findAllGestoredVocabularies(User user) {
        try {
            return em.createNativeQuery("SELECT ?vocab WHERE {"
                    + "?vocab ?jeGestorem ?user ."
                    + "}", URI.class)
                .setParameter("user", user.getUri())
                .setParameter("jeGestorem", URI.create(TermVocabulary.s_p_ma_gestora))
                .getResultStream().map(this::find).flatMap(Optional::stream).collect(Collectors.toList());
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

    /**
     * Finds vocabulary in specified vocabulary context.
     *
     * @param vocabularyUri        URI identifier of vocabulary
     * @param vocabularyContextUri URI identifier of vocabulary context
     * @return {@code Optional} containing the vocabulary instance or an empty {@code Optional} if no such instance
     *     exists.
     */
    public Optional<Vocabulary> findInContext(URI vocabularyUri, URI vocabularyContextUri) {
        Objects.requireNonNull(vocabularyUri);
        Objects.requireNonNull(vocabularyContextUri);
        try {
            return Optional.ofNullable(
                em.find(type, vocabularyUri, descriptorFactory.vocabularyDescriptor(vocabularyContextUri)));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds vocabulary with change in specified publication context.
     *
     * @param vocabularyUri         URI identifier of vocabulary
     * @param publicationContextUri URI identifier of publication context
     * @return {@code Optional} containing the vocabulary instance or an empty {@code Optional} if no such instance
     *     exists.
     */
    public Optional<Vocabulary> findFromPublicationContext(URI vocabularyUri, URI publicationContextUri) {
        Objects.requireNonNull(vocabularyUri);
        Objects.requireNonNull(publicationContextUri);
        Optional<URI> vocabularyContextUri =
            resolveContextOfVocabularyInPublicationContext(vocabularyUri, publicationContextUri);
        if (vocabularyContextUri.isEmpty()) {
            return Optional.empty();
        }
        return findInContext(vocabularyUri, vocabularyContextUri.get());
    }

    @Override
    public Vocabulary update(Vocabulary entity) {
        Objects.requireNonNull(entity);
        try {
            Vocabulary merged = em.merge(entity, descriptorFactory.vocabularyDescriptor(entity));
            em.getEntityManagerFactory().getCache().evictAll();
            return merged;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts canonical vocabularies.
     *
     * @return number of vocabularies
     */
    public int getAllCount() {
        try {
            return em.createNativeQuery("SELECT (count(?voc) as ?count) WHERE { GRAPH ?voc { ?voc a ?type . } }",
                    Integer.class)
                .setParameter("type", typeUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Counts canonical vocabularies with at least one gestor.
     *
     * @return number of vocabularies
     */
    public int getGestoredCount() {
        try {
            return em.createNativeQuery("SELECT (count(DISTINCT ?voc) as ?count) WHERE { GRAPH ?voc { "
                    + "?voc a ?type ; "
                    + "     ?gestoredBy ?gestor ."
                    + "} }", Integer.class)
                .setParameter("type", typeUri)
                .setParameter("gestoredBy", URI.create(TermVocabulary.s_p_ma_gestora))
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Gets content of specified canonical vocabulary.
     *
     * @param vocabularyUri URI of vocabulary
     * @return Jena model of vocabulary content
     */
    public Model getVocabularyContent(URI vocabularyUri) {
        ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString();
        parameterizedSparqlString.setCommandText("CONSTRUCT { ?s ?p ?o . } WHERE { "
            + "?s ?p ?o . "
            + "FILTER(?p != ?hasGestor) "
            + "}");
        parameterizedSparqlString.setIri("hasGestor", TermVocabulary.s_p_ma_gestora);
        Query query = parameterizedSparqlString.asQuery();
        query.addGraphURI(vocabularyUri.toString());
        return QueryExecution.service(repositoryConfigProperties.getUrl())
            .query(query).construct();
    }

    private Optional<URI> resolveContextOfVocabularyInPublicationContext(URI vocabularyUri,
                                                                         URI publicationContextUri) {
        Objects.requireNonNull(vocabularyUri);
        Objects.requireNonNull(publicationContextUri);
        try {
            return Optional.ofNullable(em.createNativeQuery("SELECT DISTINCT ?ctx WHERE { "
                    + "?pc ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "}", URI.class)
                .setParameter("pc", publicationContextUri)
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .setParameter("voc", vocabularyUri)
                .getSingleResult());
        } catch (NoResultException nre) {
            return Optional.empty();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
