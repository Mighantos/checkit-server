package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.VocabularyDao;
import com.github.checkit.model.Vocabulary;
import org.springframework.stereotype.Service;

@Service
public class VocabularyService extends BaseRepositoryService<Vocabulary> {

    private final VocabularyDao vocabularyDao;

    public VocabularyService(VocabularyDao vocabularyDao) {
        this.vocabularyDao = vocabularyDao;
    }

    @Override
    protected BaseDao<Vocabulary> getPrimaryDao() {
        return vocabularyDao;
    }


}
