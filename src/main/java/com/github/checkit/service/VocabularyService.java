package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.VocabularyDao;
import com.github.checkit.dto.VocabularyDto;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.model.VocabularyContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VocabularyService extends BaseRepositoryService<Vocabulary> {

    private final Logger logger = LoggerFactory.getLogger(VocabularyService.class);
    private final VocabularyDao vocabularyDao;
    private final VocabularyContextService vocabularyContextService;
    private final UserService userService;

    /**
     * Constructor.
     */
    public VocabularyService(VocabularyDao vocabularyDao, VocabularyContextService vocabularyContextService,
                             UserService userService) {
        this.vocabularyDao = vocabularyDao;
        this.vocabularyContextService = vocabularyContextService;
        this.userService = userService;
    }

    @Override
    protected BaseDao<Vocabulary> getPrimaryDao() {
        return vocabularyDao;
    }

    /**
     * Finds all vocabularies affected by changes in specified publication context.
     *
     * @param publicationContextUri URI identifier of publication context
     * @return list of vocabularies
     */
    public List<Vocabulary> findAllAffectedVocabularies(URI publicationContextUri) {
        List<Vocabulary> allAffectedVocabularies = new ArrayList<>();
        List<VocabularyContext> allAffectedVocabularyContexts =
            vocabularyContextService.findAllAffectedIn(publicationContextUri);
        for (VocabularyContext affectedVocabularyContext : allAffectedVocabularyContexts) {
            URI vocabularyUri = affectedVocabularyContext.getBasedOnVersion();
            vocabularyDao.find(vocabularyUri)
                .ifPresentOrElse(allAffectedVocabularies::add,
                    () -> vocabularyDao.findInContext(vocabularyUri, affectedVocabularyContext.getUri())
                        .ifPresentOrElse(allAffectedVocabularies::add,
                            () -> logger.warn("Vocabulary \"{}\" changed in publication context \"{}\" was not found "
                                + "in DB.", vocabularyUri, publicationContextUri)));
        }
        return allAffectedVocabularies;
    }

    /**
     * Finds all vocabularies gestored by current user.
     *
     * @return list of vocabularies
     */
    public List<VocabularyDto> getMyGestoredVocabularies() {
        User currentUser = userService.getCurrent();
        return vocabularyDao.findAllGestoredVocabularies(currentUser).stream().map(VocabularyDto::new).sorted()
            .toList();
    }

    /**
     * Finds specified vocabulary changed in specified publication context as canonical version or newly created
     * vocabulary.
     *
     * @param vocabularyUri         URI identifier of vocabulary
     * @param publicationContextUri URI identifier of publication context
     * @return vocabulary
     */
    public Vocabulary findRequiredInPublication(URI vocabularyUri, URI publicationContextUri) {
        return vocabularyDao.find(vocabularyUri)
            .orElse(vocabularyDao.findFromPublicationContext(vocabularyUri, publicationContextUri).orElseThrow(() ->
                NotFoundException.create(Vocabulary.class.getSimpleName(), vocabularyUri)));
    }

    public List<VocabularyDto> getAllInDto() {
        return findAll().stream().map(VocabularyDto::new).sorted().collect(Collectors.toList());
    }

    public int getAllCount() {
        return vocabularyDao.getAllCount();
    }

    public int getGestoredCount() {
        return vocabularyDao.getGestoredCount();
    }

    public Model getVocabularyContent(URI vocabularyUri) {
        return vocabularyDao.getVocabularyContent(vocabularyUri);
    }

    public VocabularyDto getInDto(URI uri) {
        return new VocabularyDto(findRequired(uri));
    }
}
