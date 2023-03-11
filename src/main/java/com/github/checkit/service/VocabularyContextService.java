package com.github.checkit.service;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.VocabularyContextDao;
import com.github.checkit.model.VocabularyContext;
import java.net.URI;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Service;

@Service
public class VocabularyContextService extends BaseRepositoryService<VocabularyContext> {

    private final VocabularyContextDao vocabularyContextDao;

    private final RepositoryConfigProperties repositoryConfigProperties;

    public VocabularyContextService(VocabularyContextDao vocabularyContextDao, RepositoryConfigProperties repositoryConfigProperties) {
        this.vocabularyContextDao = vocabularyContextDao;
        this.repositoryConfigProperties = repositoryConfigProperties;
    }

    @Override
    protected BaseDao<VocabularyContext> getPrimaryDao() {
        return vocabularyContextDao;
    }

    /**
     * Gets content of vocabulary in specified vocabulary context.
     *
     * @param vocabularyContextUri URI of vocabulary context
     * @return Jena model of vocabulary content
     */
    public Model getVocabularyContent(URI vocabularyContextUri) {
        Query query = QueryFactory.create("CONSTRUCT { ?s ?p ?o . } WHERE { ?s ?p ?o . }");
        query.addGraphURI(vocabularyContextUri.toString());
        Model model = QueryExecution.service(repositoryConfigProperties.getUrl())
                .query(query).construct();
        //Remove context metadata
        Resource resource = model.getResource(vocabularyContextUri.toString());
        model.remove(resource.listProperties());
        return model;
    }
}
