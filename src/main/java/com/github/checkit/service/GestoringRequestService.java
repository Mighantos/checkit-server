package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.GestoringRequestDao;
import com.github.checkit.dto.GestoringRequestDto;
import com.github.checkit.exception.AlreadyExistsException;
import com.github.checkit.model.GestoringRequest;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
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

    /**
     * Constructor.
     */
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

    public List<GestoringRequestDto> findAllAsDtos() {
        return findAll().stream().map(GestoringRequestDto::new).toList();
    }

    @Transactional
    public void remove(String id) {
        GestoringRequest gestoringRequest = findRequiredById(id);
        remove(gestoringRequest);
    }

    /**
     * Creates gestoring request for given vocabulary with current user as applicant if it does not exist yet.
     *
     * @param vocabularyUri vocabulary URI
     */
    @Transactional
    public void create(URI vocabularyUri) {
        User applicant = userService.getCurrent();
        Vocabulary vocabulary = vocabularyService.findRequired(vocabularyUri);
        if (gestoringRequestDao.exists(applicant.getUri(), vocabulary.getUri())) {
            throw new AlreadyExistsException("Gestoring request from user \"%s\" to vocabulary \"%s\" already exists.",
                applicant.getFullName(), vocabulary.getLabel());
        }
        persist(new GestoringRequest(applicant, vocabulary));
    }

    /**
     * Resolves gestoring request by adding applicant as gestor of vocabulary if approved and deleting gestoring
     * request.
     *
     * @param requestId ID of gestoring request
     * @param approved  if the gestoring request was approved or not
     */
    @Transactional
    public void resolveGestoringRequest(String requestId, boolean approved) {
        if (approved) {
            GestoringRequest gestoringRequest = findRequiredById(requestId);
            Vocabulary vocabulary = gestoringRequest.getVocabulary();
            User applicant = gestoringRequest.getApplicant();
            vocabulary.addGestor(applicant);
            vocabularyService.update(vocabulary);
        }
        remove(requestId);
    }

    private URI createUriFromId(String id) {
        return URI.create(TermVocabulary.s_c_pozadavek_na_gestorovani + "/" + id);
    }

    public int getAllCount() {
        return gestoringRequestDao.getAllCount();
    }
}
