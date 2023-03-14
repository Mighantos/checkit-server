package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.VocabularyDao;
import com.github.checkit.dto.VocabularyDto;
import com.github.checkit.dto.VocabularyInfoDto;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class VocabularyService extends BaseRepositoryService<Vocabulary> {

    private final VocabularyDao vocabularyDao;
    private final UserService userService;

    public VocabularyService(VocabularyDao vocabularyDao, UserService userService) {
        this.vocabularyDao = vocabularyDao;
        this.userService = userService;
    }

    @Override
    protected BaseDao<Vocabulary> getPrimaryDao() {
        return vocabularyDao;
    }


    public List<VocabularyDto> getAllInDto() {
        return findAll().stream().map(VocabularyDto::new)
            .sorted(Comparator.comparing(VocabularyDto::getLabel)).collect(Collectors.toList());
    }

    public int getAllCount() {
        return vocabularyDao.getAllCount();
    }

    public int getGestoredCount() {
        return vocabularyDao.getGestoredCount();
    }

    /**
     * Finds vocabularies gestored by current user.
     *
     * @return list of gestored vocabularies
     */
    public List<VocabularyInfoDto> getMyGestoredVocabularies() {
        User currentUser = userService.getCurrent();
        return vocabularyDao.findGestoredVocabularies(currentUser).stream().map(VocabularyInfoDto::new)
            .sorted(Comparator.comparing(VocabularyInfoDto::getLabel)).toList();
    }
}
