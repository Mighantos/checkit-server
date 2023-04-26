package com.github.checkit.dao;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Repository;

@Repository
public class VocabularyContextDao extends BaseDao<VocabularyContext> {

    private final RepositoryConfigProperties repositoryConfigProperties;

    protected VocabularyContextDao(EntityManager em, RepositoryConfigProperties repositoryConfigProperties) {
        super(VocabularyContext.class, em);
        this.repositoryConfigProperties = repositoryConfigProperties;
    }

    /**
     * Finds affected vocabulary contexts of specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @return list of URIs of vocabulary contexts
     */
    public List<URI> findAllAffectedIn(URI publicationContextUri) {
        Objects.requireNonNull(publicationContextUri);
        try {
            return em.createNativeQuery("SELECT DISTINCT ?ctx WHERE {"
                    + "?pc a ?type ; "
                    + "    ?hasChange ?change . "
                    + "?change ?inContext ?ctx . "
                    + "?ctx ?basedOn ?voc . "
                    + "}", URI.class)
                .setParameter("pc", publicationContextUri)
                .setParameter("type", URI.create(TermVocabulary.s_c_publikacni_kontext))
                .setParameter("hasChange", URI.create(TermVocabulary.s_p_ma_zmenu))
                .setParameter("inContext", URI.create(TermVocabulary.s_p_v_kontextu))
                .setParameter("basedOn", URI.create(TermVocabulary.s_p_vychazi_z_verze))
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Gets content of vocabulary in specified vocabulary context.
     *
     * @param vocabularyContextUri URI of vocabulary context
     * @return Jena model of vocabulary content
     */
    public Model getVocabularyContent(URI vocabularyContextUri) {
        ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString();
        parameterizedSparqlString.setCommandText("CONSTRUCT { ?s ?p ?o . } WHERE { "
            + "?s ?p ?o . "
            + "FILTER(?s != ?vc)"
            + "}");
        parameterizedSparqlString.setIri("vc", vocabularyContextUri.toString());
        Query query = parameterizedSparqlString.asQuery();
        query.addGraphURI(vocabularyContextUri.toString());
        return QueryExecution.service(repositoryConfigProperties.getUrl())
            .query(query).construct();
    }
}
