package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.VocabularyContextDao;
import com.github.checkit.model.VocabularyContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VocabularyContextService extends BaseRepositoryService<VocabularyContext> {

    private final Logger logger = LoggerFactory.getLogger(VocabularyContextService.class);
    private final VocabularyContextDao vocabularyContextDao;

    public VocabularyContextService(VocabularyContextDao vocabularyContextDao) {
        this.vocabularyContextDao = vocabularyContextDao;
    }

    @Override
    protected BaseDao<VocabularyContext> getPrimaryDao() {
        return vocabularyContextDao;
    }

    /**
     * Finds all vocabulary contexts affected by changes in specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @return list of vocabulary contexts
     */
    public List<VocabularyContext> findAllAffectedIn(URI publicationContextUri) {
        List<VocabularyContext> affectedContexts = new ArrayList<>();
        for (URI affectedContextUri : vocabularyContextDao.findAllAffectedIn(publicationContextUri)) {
            Optional<VocabularyContext> optContext = this.find(affectedContextUri);
            optContext.ifPresentOrElse(affectedContexts::add,
                () -> logger.warn(
                    "Vocabulary context \"{}\" with change in publication context \"{}\" was not found in DB.",
                    affectedContextUri, publicationContextUri));
        }
        return affectedContexts;
    }

    public Model getVocabularyContent(URI vocabularyContextUri) {
        return vocabularyContextDao.getVocabularyContent(vocabularyContextUri);
    }
}
