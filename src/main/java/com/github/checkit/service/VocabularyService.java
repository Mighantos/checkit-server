package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.VocabularyDao;
import com.github.checkit.dto.VocabularyDto;
import com.github.checkit.model.Vocabulary;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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


    public List<VocabularyDto> getAllInDto() {
        return findAll().stream().map(VocabularyDto::new)
            .sorted(Comparator.comparing(VocabularyDto::getUri)).collect(Collectors.toList());
    }

    public int getAllCount() {
        return vocabularyDao.getAllCount();
    }

    public int getGestoredCount() {
        return vocabularyDao.getGestoredCount();
    }
}
