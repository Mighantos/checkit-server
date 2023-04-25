package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.VocabularyContextDao;
import com.github.checkit.model.VocabularyContext;
import java.net.URI;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;

@Service
public class VocabularyContextService extends BaseRepositoryService<VocabularyContext> {

    private final VocabularyContextDao vocabularyContextDao;

    public VocabularyContextService(VocabularyContextDao vocabularyContextDao) {
        this.vocabularyContextDao = vocabularyContextDao;
    }

    @Override
    protected BaseDao<VocabularyContext> getPrimaryDao() {
        return vocabularyContextDao;
    }

    public Model getVocabularyContent(URI vocabularyContextUri) {
        return vocabularyContextDao.getVocabularyContent(vocabularyContextUri);
    }
}
