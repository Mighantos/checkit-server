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
