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
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestoringRequestService extends BaseRepositoryService<GestoringRequest> {

    private final Logger logger = LoggerFactory.getLogger(GestoringRequestService.class);

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

    @Transactional(readOnly = true)
    public List<GestoringRequestDto> findAllRequestsAsDto() {
        return findAll().stream()
            .map(gr -> new GestoringRequestDto(gr, vocabularyService.findRequired(gr.getVocabulary()))).toList();
    }

    /**
     * Finds gestoring requests created by current user.
     *
     * @return list of gestoring requests
     */
    @Transactional(readOnly = true)
    public List<GestoringRequestDto> findMyRequestsAsDto() {
        URI currentUserUri = userService.getCurrent().getUri();
        return gestoringRequestDao.findAllFromApplicant(currentUserUri).stream().map(gr -> new GestoringRequestDto(gr,
            vocabularyService.findRequired(gr.getVocabulary()))).toList();
    }

    public Optional<GestoringRequest> findById(String id) {
        URI uri = createUriFromId(id);
        return find(uri);
    }

    public GestoringRequest findRequiredById(String id) {
        URI uri = createUriFromId(id);
        return findRequired(uri);
    }

    /**
     * Creates gestoring request for given vocabulary with current user as applicant if it does not exist or user
     * already does not gestor the vocabulary already.
     *
     * @param vocabularyUri vocabulary URI
     */
    @Transactional
    public void create(URI vocabularyUri) {
        User applicant = userService.getCurrent();
        Vocabulary vocabulary = vocabularyService.findRequired(vocabularyUri);
        if (vocabulary.getGestors().contains(applicant)) {
            throw new AlreadyExistsException("User \"%s\" already gestors vocabulary \"%s\".",
                applicant.getFullName(), vocabulary.getLabel());
        }
        if (gestoringRequestDao.exists(applicant.getUri(), vocabulary.getUri())) {
            throw new AlreadyExistsException("Gestoring request from user \"%s\" to vocabulary \"%s\" already exists.",
                applicant.getFullName(), vocabulary.getLabel());
        }
        persist(new GestoringRequest(applicant, vocabulary));
        logger.info("Gestoring request from user \"{}\" to vocabulary \"{}\" was created.", applicant.toSimpleString(),
            vocabulary.getUri());
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
        GestoringRequest gestoringRequest = findRequiredById(requestId);
        Vocabulary vocabulary = vocabularyService.findRequired(gestoringRequest.getVocabulary());
        User applicant = userService.findRequired(gestoringRequest.getApplicant().getUri());
        if (approved) {
            vocabulary.addGestor(applicant);
            vocabularyService.update(vocabulary);
        }
        remove(gestoringRequest);
        logger.info("Gestoring request \"{}\" from user \"{}\" to vocabulary \"{}\" was {}.",
            gestoringRequest.getUri(), applicant.toSimpleString(), vocabulary.getUri(),
            approved ? "approved" : "rejected");
    }

    /**
     * Remove specified gestoring request.
     *
     * @param id identifier of gestoring request
     */
    @Transactional
    public void remove(String id) {
        Optional<GestoringRequest> gestoringRequest = findById(id);
        gestoringRequest.ifPresent(gr -> {
            remove(gr);
            logger.info("Gestoring request \"{}\" of user {} to vocabulary \"{}\" was removed.", gr.getUri(),
                gr.getApplicant().toSimpleString(), gr.getVocabulary());
        });
    }

    /**
     * Remove gestoring request of specified user to specified vocabulary.
     *
     * @param vocabularyUri URI identifier of vocabulary
     * @param applicantUri  URI identifier of user
     */
    @Transactional
    public void remove(URI vocabularyUri, URI applicantUri) {
        Optional<GestoringRequest> gestoringRequest = gestoringRequestDao.find(vocabularyUri, applicantUri);
        gestoringRequest.ifPresent(gr -> {
            remove(gr);
            logger.info("Gestoring request \"{}\" of user {} to vocabulary \"{}\" was removed.", gr.getUri(),
                gr.getApplicant().toSimpleString(), gr.getVocabulary());
        });
    }

    public int getAllCount() {
        return gestoringRequestDao.getAllCount();
    }

    private URI createUriFromId(String id) {
        return URI.create(TermVocabulary.s_c_pozadavek_na_gestorovani + "/" + id);
    }
}
