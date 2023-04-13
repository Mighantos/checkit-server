package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.ChangeDao;
import com.github.checkit.exception.ForbiddenException;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.model.AbstractChangeableContext;
import com.github.checkit.model.Change;
import com.github.checkit.model.User;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.service.auxiliary.ChangeResolver;
import com.github.checkit.util.TermVocabulary;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeService extends BaseRepositoryService<Change> {
    private final VocabularyService vocabularyService;
    private final VocabularyContextService vocabularyContextService;
    private final UserService userService;
    private final ChangeDao changeDao;

    /**
     * Constructor.
     */
    public ChangeService(VocabularyService vocabularyService, VocabularyContextService vocabularyContextService,
                         UserService userService, ChangeDao changeDao) {
        this.vocabularyService = vocabularyService;
        this.vocabularyContextService = vocabularyContextService;
        this.userService = userService;
        this.changeDao = changeDao;
    }

    @Override
    protected BaseDao<Change> getPrimaryDao() {
        return changeDao;
    }

    /**
     * Marks specified change as approved by current user.
     *
     * @param changeId identifier of change
     */
    @Transactional
    public void approveChange(String changeId) {
        User current = userService.getCurrent();
        URI changeUri = createChangeUriFromId(changeId);
        checkUserCanReviewChange(current.getUri(), changeUri);
        Change change = findRequired(changeUri);
        change.addApprovedBy(current);
        change.removeRejectedBy(current);
        changeDao.update(change);
    }

    /**
     * Marks specified list of changes as approved by current user.
     *
     * @param changeUris URI identifiers of changes
     */
    @Transactional
    public void approveChanges(List<URI> changeUris) {
        User current = userService.getCurrent();
        changeUris.forEach(changeUri -> checkUserCanReviewChange(current.getUri(), changeUri));
        for (URI changeUri : changeUris) {
            Change change = findRequired(changeUri);
            change.addApprovedBy(current);
            change.removeRejectedBy(current);
            changeDao.update(change);
        }
    }

    /**
     * Marks specified change as approved by current user.
     *
     * @param changeId identifier of change
     */
    @Transactional
    public void rejectChange(String changeId) {
        User current = userService.getCurrent();
        URI changeUri = createChangeUriFromId(changeId);
        checkUserCanReviewChange(current.getUri(), changeUri);
        Change change = findRequired(changeUri);
        change.addRejectedBy(current);
        change.removeApprovedBy(current);
        changeDao.update(change);
    }

    /**
     * Marks specified list of changes as rejected by current user.
     *
     * @param changeUris URI identifiers of changes
     */
    @Transactional
    public void rejectChanges(List<URI> changeUris) {
        User current = userService.getCurrent();
        changeUris.forEach(changeUri -> checkUserCanReviewChange(current.getUri(), changeUri));
        for (URI changeUri : changeUris) {
            Change change = findRequired(changeUri);
            change.addRejectedBy(current);
            change.removeApprovedBy(current);
            changeDao.update(change);
        }
    }

    /**
     * Clears current user's review on specified change.
     *
     * @param changeId identifier of change
     */
    @Transactional
    public void removeChangeReview(String changeId) {
        User current = userService.getCurrent();
        URI changeUri = createChangeUriFromId(changeId);
        checkUserCanReviewChange(current.getUri(), changeUri);
        Change change = findRequired(changeUri);
        change.removeRejectedBy(current);
        change.removeApprovedBy(current);
        changeDao.update(change);
    }

    /**
     * Clears current user's review on specified list of changes.
     *
     * @param changeUris URI identifiers of changes
     */
    @Transactional
    public void removeChangesReview(List<URI> changeUris) {
        User current = userService.getCurrent();
        changeUris.forEach(changeUri -> checkUserCanReviewChange(current.getUri(), changeUri));
        for (URI changeUri : changeUris) {
            Change change = findRequired(changeUri);
            change.removeRejectedBy(current);
            change.removeApprovedBy(current);
            changeDao.update(change);
        }
    }

    /**
     * Returns list of changes made in specified vocabulary context compared to its canonical version.
     *
     * @param vocabularyContext {@link VocabularyContext} to find changes in
     * @return list of changes
     */
    public List<Change> getChanges(VocabularyContext vocabularyContext) {
        Model canonicalGraph =
            vocabularyService.getVocabularyContent(vocabularyContext.getBasedOnVocabulary().getUri());
        Model draftGraph = vocabularyContextService.getVocabularyContent(vocabularyContext.getUri());
        return getChanges(canonicalGraph, draftGraph, vocabularyContext);
    }

    /**
     * Returns list of changes made in specified draft compared to provided canonical version.
     *
     * @param canonicalGraph            Model representation of canonical graph (context)
     * @param draftGraph                Model representation of draft graph (context)
     * @param abstractChangeableContext context the changes were made in
     * @return list of changes
     */
    public List<Change> getChanges(Model canonicalGraph, Model draftGraph,
                                   AbstractChangeableContext abstractChangeableContext) {
        if (canonicalGraph.isIsomorphicWith(draftGraph)) {
            return new ArrayList<>();
        }
        ChangeResolver changeResolver = new ChangeResolver(canonicalGraph, draftGraph, abstractChangeableContext,
            changeDao);
        changeResolver.findChangesInStatementsWithoutBlankNode();
        changeResolver.findChangesInSubGraphs();
        return changeResolver.getChanges();
    }

    private void checkUserCanReviewChange(URI userUri, URI changeUri) {
        checkExists(changeUri);
        if (!changeDao.isUserGestorOfVocabularyWithChange(userUri, changeUri)) {
            throw ForbiddenException.createForbiddenToReview(userUri, changeUri);
        }
    }

    private URI createChangeUriFromId(String id) {
        return URI.create(TermVocabulary.s_c_zmena + "/" + id);
    }

    private void checkExists(URI changeUri) {
        if (!exists(changeUri)) {
            throw NotFoundException.create(Change.class, changeUri);
        }
    }
}
