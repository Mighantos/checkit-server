package com.github.checkit.service;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.VocabularyDao;
import com.github.checkit.dto.VocabularyDto;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;

@Service
public class VocabularyService extends BaseRepositoryService<Vocabulary> {

    private final VocabularyDao vocabularyDao;
    private final UserService userService;
    private final RepositoryConfigProperties repositoryConfigProperties;

    public VocabularyService(VocabularyDao vocabularyDao, UserService userService) {
        this.vocabularyDao = vocabularyDao;
        this.userService = userService;
        this.repositoryConfigProperties = repositoryConfigProperties;
    }

    @Override
    protected BaseDao<Vocabulary> getPrimaryDao() {
        return vocabularyDao;
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

    public List<VocabularyDto> getMyGestoredVocabularies() {
        User currentUser = userService.getCurrent();
        return vocabularyDao.findGestoredVocabularies(currentUser).stream().map(VocabularyDto::new).sorted().toList();
    }

    /**
     * Gets content of specified canonical vocabulary.
     *
     * @param vocabularyUri URI of vocabulary
     * @return Jena model of vocabulary content
     */
    public Model getVocabularyContent(URI vocabularyUri) {
        Query query = QueryFactory.create("CONSTRUCT { ?s ?p ?o . } WHERE { ?s ?p ?o . }");
        query.addGraphURI(vocabularyUri.toString());
        return QueryExecution.service(repositoryConfigProperties.getUrl())
                .query(query).construct();
    }

    public VocabularyDto getInDto(URI uri) {
        return new VocabularyDto(findRequired(uri));
    }
}
