package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.ChangeDao;
import com.github.checkit.dao.CommentDao;
import com.github.checkit.exception.EmptyArrayParameterException;
import com.github.checkit.exception.ForbiddenException;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.exception.PublicationContextIsClosedException;
import com.github.checkit.exception.PublicationContextWasUpdatedException;
import com.github.checkit.model.Change;
import com.github.checkit.model.User;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.model.auxilary.AbstractChangeableContext;
import com.github.checkit.service.auxiliary.ChangeResolver;
import com.github.checkit.util.TermVocabulary;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeService extends BaseRepositoryService<Change> {

    private final Logger logger = LoggerFactory.getLogger(ChangeService.class);

    private final VocabularyService vocabularyService;
    private final VocabularyContextService vocabularyContextService;
    private final UserService userService;
    private final ChangeDao changeDao;
    private final CommentDao commentDao;

    /**
     * Constructor.
     */
    public ChangeService(VocabularyService vocabularyService, VocabularyContextService vocabularyContextService,
                         UserService userService, ChangeDao changeDao, CommentDao commentDao) {
        this.vocabularyService = vocabularyService;
        this.vocabularyContextService = vocabularyContextService;
        this.userService = userService;
        this.changeDao = changeDao;
        this.commentDao = commentDao;
    }

    @Override
    protected BaseDao<Change> getPrimaryDao() {
        return changeDao;
    }

    /**
     * Finds any change in context in publication context that is not ROLLBACKED.
     *
     * @param publicationContextUri URI identifier of publication context
     * @param contextUri            URI identifier of context
     * @return change
     */
    public Change findRequiredAnyInContextInPublicationContext(URI publicationContextUri, URI contextUri) {
        return changeDao.findAnyInContextInPublicationContext(publicationContextUri, contextUri).orElseThrow(() ->
            new NotFoundException("No changes for context \"%s\" found in publication context \"%s\".",
                contextUri, publicationContextUri));
    }

    /**
     * Marks specified change as approved by current user.
     *
     * @param changeId    identifier of change
     * @param versionDate date of publication context last update
     */
    @Transactional
    public void approveChange(String changeId, Instant versionDate) {
        User current = userService.getCurrent();
        URI changeUri = createChangeUriFromId(changeId);
        checkUserCanReviewChange(current.getUri(), changeUri);
        checkUpToDate(changeUri, versionDate);
        Change change = findRequired(changeUri);
        change.addApprovedBy(current);
        change.removeRejectedBy(current);
        changeDao.update(change);
        logger.info("User {} approved change \"{}\".", current.toSimpleString(), changeUri);
    }

    /**
     * Marks specified list of changes as approved by current user.
     *
     * @param changeUris  URI identifiers of changes
     * @param versionDate date of publication context last update
     */
    @Transactional
    public void approveChanges(List<URI> changeUris, Instant versionDate) {
        User current = userService.getCurrent();
        if (changeUris.isEmpty()) {
            throw new EmptyArrayParameterException("List of changes to approve can't be empty.");
        }
        changeUris.forEach(changeUri -> checkUserCanReviewChange(current.getUri(), changeUri));
        checkUpToDate(changeUris.get(0), versionDate);
        for (URI changeUri : changeUris) {
            Change change = findRequired(changeUri);
            change.addApprovedBy(current);
            change.removeRejectedBy(current);
            changeDao.update(change);
        }
        logger.info("User {} approved changes: {}.", current.toSimpleString(), changeUris);
    }

    /**
     * Marks specified change as approved by current user.
     *
     * @param changeId    identifier of change
     * @param versionDate date of publication context last update
     */
    @Transactional
    public void rejectChange(String changeId, Instant versionDate) {
        User current = userService.getCurrent();
        URI changeUri = createChangeUriFromId(changeId);
        checkUserCanReviewChange(current.getUri(), changeUri);
        checkUpToDate(changeUri, versionDate);
        Change change = findRequired(changeUri);
        change.addRejectedBy(current);
        change.removeApprovedBy(current);
        changeDao.update(change);
        logger.info("User {} rejected change \"{}\".", current.toSimpleString(), changeUri);
    }

    /**
     * Marks specified list of changes as rejected by current user.
     *
     * @param changeUris  URI identifiers of changes
     * @param versionDate date of publication context last update
     */
    @Transactional
    public void rejectChanges(List<URI> changeUris, Instant versionDate) {
        User current = userService.getCurrent();
        if (changeUris.isEmpty()) {
            throw new EmptyArrayParameterException("List of changes to reject can't be empty.");
        }
        changeUris.forEach(changeUri -> checkUserCanReviewChange(current.getUri(), changeUri));
        checkUpToDate(changeUris.get(0), versionDate);
        for (URI changeUri : changeUris) {
            Change change = findRequired(changeUri);
            change.addRejectedBy(current);
            change.removeApprovedBy(current);
            changeDao.update(change);
        }
        logger.info("User {} rejected changes: {}.", current.toSimpleString(), changeUris);
    }

    /**
     * Clears current user's review on specified change.
     *
     * @param changeId    identifier of change
     * @param versionDate date of publication context last update
     */
    @Transactional
    public void removeChangeReview(String changeId, Instant versionDate) {
        User current = userService.getCurrent();
        URI changeUri = createChangeUriFromId(changeId);
        checkUserCanReviewChange(current.getUri(), changeUri);
        checkUpToDate(changeUri, versionDate);
        Change change = findRequired(changeUri);
        change.removeRejectedBy(current);
        change.removeApprovedBy(current);
        changeDao.update(change);
        commentDao.findFinalComment(change, current).ifPresent(commentDao::remove);
        logger.info("Review of user {} was cleared on change \"{}\".", current.toSimpleString(), changeUri);
    }

    /**
     * Clears current user's review on specified list of changes.
     *
     * @param changeUris  URI identifiers of changes
     * @param versionDate date of publication context last update
     */
    @Transactional
    public void removeChangesReview(List<URI> changeUris, Instant versionDate) {
        User current = userService.getCurrent();
        if (changeUris.isEmpty()) {
            throw new EmptyArrayParameterException("List of changes to clear review can't be empty.");
        }
        changeUris.forEach(changeUri -> checkUserCanReviewChange(current.getUri(), changeUri));
        checkUpToDate(changeUris.get(0), versionDate);
        for (URI changeUri : changeUris) {
            Change change = findRequired(changeUri);
            change.removeRejectedBy(current);
            change.removeApprovedBy(current);
            changeDao.update(change);
            commentDao.findFinalComment(change, current).ifPresent(commentDao::remove);
        }
        logger.info("Review of user {} was cleared on changes: {}.", current.toSimpleString(), changeUris);
    }

    /**
     * Returns list of changes made in specified vocabulary context compared to its canonical version.
     *
     * @param vocabularyContext {@link VocabularyContext} to find changes in
     * @return list of changes
     */
    public List<Change> getChanges(VocabularyContext vocabularyContext) {
        Model canonicalGraph =
            vocabularyService.getVocabularyContent(vocabularyContext.getBasedOnVersion());
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

    /**
     * Check if specified user can review specified change.
     *
     * @param userUri   URI identifier of user
     * @param changeUri URI identifier of change
     */
    public void checkUserCanReviewChange(URI userUri, URI changeUri) {
        checkExists(changeUri);
        checkNotInClosedPublicationContext(changeUri);
        if (!userService.isCurrentAdmin() && !changeDao.isUserGestorOfVocabularyWithChange(userUri, changeUri)) {
            throw ForbiddenException.createForbiddenToReviewChange(userUri, changeUri);
        }
    }

    /**
     * Generates unique URI for change entity.
     *
     * @return URI identifier
     */
    public URI generateEntityUri() {
        return changeDao.generateEntityUri();
    }

    private void checkNotInClosedPublicationContext(URI changeUri) {
        if (changeDao.isChangesPublicationContextClosed(changeUri)) {
            throw PublicationContextIsClosedException.create(changeUri);
        }
    }

    private void checkUpToDate(URI changeUri, Instant versionDate) {
        if (changeDao.wasChangesPublicationContextUpdated(changeUri, versionDate)) {
            throw PublicationContextWasUpdatedException.create(changeUri);
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
