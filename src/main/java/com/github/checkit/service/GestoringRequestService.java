package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.GestoringRequestDao;
import com.github.checkit.dto.GestoringRequestDto;
import com.github.checkit.model.GestoringRequest;
import com.github.checkit.util.TermVocabulary;
import java.net.URI;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestoringRequestService extends BaseRepositoryService<GestoringRequest> {

    private final GestoringRequestDao gestoringRequestDao;
    private final UserService userService;
    private final VocabularyService vocabularyService;

    public GestoringRequestService(GestoringRequestDao gestoringRequestDao, UserService userService,
                                   VocabularyService vocabularyService) {
        this.gestoringRequestDao = gestoringRequestDao;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
    }

    @Override
    protected BaseDao<GestoringRequest> getPrimaryDao() {
        return gestoringRequestDao;
    }

    public GestoringRequest findRequiredById(String id) {
        URI uri = createUriFromId(id);
        return findRequired(uri);
    }

    private URI createUriFromId(String id) {
        return URI.create(TermVocabulary.s_c_pozadavek_na_gestorovani + "/" + id);
    }

    public List<GestoringRequestDto> findAllAsDtos() {
        return findAll().stream().map(GestoringRequestDto::new).toList();
    }

    @Transactional
    public void remove(String id) {
        GestoringRequest gestoringRequest = findRequiredById(id);
        remove(gestoringRequest);
    }

    @Transactional
    public void create(URI vocabularyUri) {
        GestoringRequest gestoringRequest = new GestoringRequest();
        gestoringRequest.setApplicant(userService.getCurrent());
        gestoringRequest.setVocabulary(vocabularyService.findRequired(vocabularyUri));
        persist(gestoringRequest);
    }
}
