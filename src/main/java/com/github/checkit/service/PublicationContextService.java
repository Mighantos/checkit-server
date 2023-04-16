package com.github.checkit.service;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.PublicationContextDao;
import com.github.checkit.dto.ChangeDto;
import com.github.checkit.dto.ContextChangesDto;
import com.github.checkit.dto.PublicationContextDetailDto;
import com.github.checkit.dto.PublicationContextDto;
import com.github.checkit.dto.ReviewableVocabularyDto;
import com.github.checkit.dto.auxiliary.PublicationContextState;
import com.github.checkit.exception.AlreadyExistsException;
import com.github.checkit.exception.ForbiddenException;
import com.github.checkit.exception.NoChangeException;
import com.github.checkit.exception.NotApprovableException;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.Comment;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.model.auxilary.AbstractChangeableContext;
import com.github.checkit.model.auxilary.CommentTag;
import com.github.checkit.service.auxiliary.ChangeDtoComposer;
import com.github.checkit.util.TermVocabulary;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicationContextService extends BaseRepositoryService<PublicationContext> {

    private final Logger logger = LoggerFactory.getLogger(PublicationContextService.class);

    private final PublicationContextDao publicationContextDao;
    private final ProjectContextService projectContextService;
    private final ChangeService changeService;
    private final VocabularyService vocabularyService;
    private final UserService userService;
    private final CommentService commentService;
    private final String defaultLanguageTag;

    /**
     * Construct.
     */
    public PublicationContextService(PublicationContextDao publicationContextDao,
                                     ProjectContextService projectContextService, ChangeService changeService,
                                     VocabularyService vocabularyService, UserService userService,
                                     CommentService commentService,
                                     RepositoryConfigProperties repositoryConfigProperties) {
        this.publicationContextDao = publicationContextDao;
        this.projectContextService = projectContextService;
        this.changeService = changeService;
        this.vocabularyService = vocabularyService;
        this.userService = userService;
        this.commentService = commentService;
        this.defaultLanguageTag = repositoryConfigProperties.getLanguage();
    }

    @Override
    protected BaseDao<PublicationContext> getPrimaryDao() {
        return publicationContextDao;
    }

    /**
     * Get publication contexts that current user can't review.
     *
     * @return list of publication contexts
     */
    @Transactional
    public List<PublicationContextDto> getReadonlyPublicationContexts() {
        List<PublicationContext> allPublicationContexts = findAll();
        URI userUri = userService.getCurrent().getUri();
        allPublicationContexts.removeAll(publicationContextDao.findAllThatAffectVocabulariesGestoredBy(userUri));
        return allPublicationContexts.stream().map(pc -> {
            PublicationContextState state = getState(pc, null);
            return new PublicationContextDto(pc, state);
        }).toList();
    }

    /**
     * Gets list of publication contexts relevant to current user.
     *
     * @return list of publication contexts
     */
    @Transactional
    public List<PublicationContextDto> getReviewablePublicationContexts() {
        User current = userService.getCurrent();
        List<PublicationContext> publicationContexts =
            publicationContextDao.findAllThatAffectVocabulariesGestoredBy(current.getUri());
        return publicationContexts.stream().map(pc -> {
            PublicationContextState state = getState(pc, current);
            return new PublicationContextDto(pc, state);
        }).toList();
    }

    /**
     * Get detail of publication context specified by id.
     *
     * @param publicationContextId identifier of publication context
     * @return publication context detail
     */
    @Transactional
    public PublicationContextDetailDto getPublicationContextDetail(String publicationContextId) {
        User current = userService.getCurrent();
        URI publicationContextUri = createPublicationContextUriFromId(publicationContextId);

        PublicationContext pc = findRequired(publicationContextUri);
        PublicationContextState state = getState(pc, current);
        List<ReviewableVocabularyDto> affectedVocabularies =
            vocabularyService.findAllAffectedVocabularies(pc.getUri()).stream()
                .map(vocabulary -> new ReviewableVocabularyDto(vocabulary, vocabulary.getGestors().contains(current)))
                .toList();
        return new PublicationContextDetailDto(pc, state, affectedVocabularies);
    }

    /**
     * Get changes made in specified vocabulary in specified publication context.
     *
     * @param publicationContextId identifier of publication context
     * @param vocabularyUri        URI identifier of vocabulary
     * @param language             preferred language
     * @return basic information about context and changes made
     */
    @Transactional
    public ContextChangesDto getChangesInContextInPublicationContext(String publicationContextId, URI vocabularyUri,
                                                                     String language) {
        User current = userService.getCurrent();
        URI publicationContextUri = createPublicationContextUriFromId(publicationContextId);
        boolean allowedToReview =
            publicationContextDao.doesUserHavePermissionToReviewVocabulary(current.getUri(), publicationContextUri,
                vocabularyUri);

        PublicationContext pc = findRequired(publicationContextUri);
        String vocabularyLabel = vocabularyService.findRequired(vocabularyUri).getLabel();
        List<ChangeDto> changes = convertPublicationChangesToDtos(pc, current, language, vocabularyUri);
        return new ContextChangesDto(vocabularyUri, vocabularyLabel, allowedToReview, changes);
    }

    /**
     * Create (or update if already exists) publication context with all changes made in specified project compared to
     * canonical version of its vocabularies and attachments.
     *
     * @param projectUri project URI identifier
     */
    @Transactional
    public URI createOrUpdatePublicationContext(URI projectUri) {
        ProjectContext project = projectContextService.findRequired(projectUri);
        List<Change> currentChanges = new ArrayList<>();
        for (VocabularyContext vocabularyContext : project.getVocabularyContexts()) {
            currentChanges.addAll(changeService.getChanges(vocabularyContext));
        }

        PublicationContext publicationContext;
        boolean publicationContextExists = publicationContextDao.exists(project);
        if (publicationContextExists) {
            publicationContext = findRequiredFromProject(project);
        } else {
            //Don't create publication context with no changes.
            if (currentChanges.isEmpty()) {
                throw new NoChangeException();
            }

            publicationContext = new PublicationContext();
            publicationContext.setFromProject(project);
        }

        Set<Change> newFormOfChanges =
            takeIntoConsiderationExistingChanges(currentChanges, publicationContext.getChanges());
        publicationContext.setChanges(newFormOfChanges);

        URI publicationContextUri;
        if (publicationContextExists) {
            publicationContextUri = update(publicationContext).getUri();
            logger.info("Changes in publication context \"{}\" were updated from project \"{}\".",
                publicationContextUri, projectUri);
        } else {
            persist(publicationContext);
            publicationContextUri = publicationContext.getUri();
            logger.info("Publication context \"{}\" was created from project \"{}\".", publicationContextUri,
                projectUri);
        }
        return publicationContextUri;
    }

    /**
     * Creates an approval comment on Publication context.
     *
     * @param publicationContextId Identifier of publication context
     * @param finalComment         Content of final comment
     */
    @Transactional
    public void approvePublicationContext(String publicationContextId, String finalComment) {
        User current = userService.getCurrent();
        URI publicationContextUri = createPublicationContextUriFromId(publicationContextId);
        PublicationContext publicationContext = findRequired(publicationContextUri);
        checkNotAlreadyReviewed(publicationContext);
        checkCanReview(publicationContext, current);
        if (!isApprovable(publicationContext, current)) {
            throw NotApprovableException.create(publicationContext.getUri());
        }
        Comment comment = new Comment();
        comment.setTopic(publicationContext);
        comment.setTag(CommentTag.APPROVAL);
        comment.setAuthor(current);
        comment.setContent(finalComment);
        commentService.persist(comment);
    }

    /**
     * Creates a rejection comment on Publication context.
     *
     * @param publicationContextId Identifier of publication context
     * @param finalComment         Content of final comment
     */
    @Transactional
    public void rejectPublicationContext(String publicationContextId, String finalComment) {
        User current = userService.getCurrent();
        URI publicationContextUri = createPublicationContextUriFromId(publicationContextId);
        PublicationContext publicationContext = findRequired(publicationContextUri);
        checkNotAlreadyReviewed(publicationContext);
        checkCanReview(publicationContext, current);
        Comment comment = new Comment();
        comment.setTopic(publicationContext);
        comment.setTag(CommentTag.REJECTION);
        comment.setAuthor(current);
        comment.setContent(finalComment);
        commentService.persist(comment);

    }

    private List<ChangeDto> convertPublicationChangesToDtos(PublicationContext pc, User current, String language,
                                                            URI vocabularyUri) {
        Set<Change> changes = pc.getChanges();
        List<ChangeDto> changeDtos = new ArrayList<>(changes.stream().filter(change ->
                ((VocabularyContext) change.getContext()).getBasedOnVocabulary().getUri().equals(vocabularyUri))
            .map(change -> new ChangeDto(change, current, language, defaultLanguageTag)).toList());
        if (changeDtos.isEmpty()) {
            throw new NotFoundException("No changes in vocabulary \"%s\" found in publication context \"%s\".",
                vocabularyUri, pc.getUri());
        }
        ChangeDtoComposer changeDtoComposer = new ChangeDtoComposer(changeDtos);
        changeDtoComposer.compose();
        changeDtos.addAll(changeDtoComposer.getGroupChangeDtosOfRestrictions());

        return changeDtos.stream().sorted().toList();
    }

    private void checkCanReview(PublicationContext pc, User user) {
        if (!publicationContextDao.canUserReview(pc.getUri(), user.getUri())) {
            throw ForbiddenException.createForbiddenToReviewPublicationContext(user.getUri(), pc.getUri());
        }
    }

    private void checkNotAlreadyReviewed(PublicationContext pc) {
        if (commentService.findFinalComment(pc).isPresent()) {
            throw new AlreadyExistsException("Publication context \"%s\" was already reviewed.", pc.getUri());
        }
    }

    private URI createPublicationContextUriFromId(String id) {
        return URI.create(TermVocabulary.s_c_publikacni_kontext + "/" + id);
    }

    private PublicationContextState getState(PublicationContext pc, User current) {
        Optional<Comment> optComment = commentService.findFinalComment(pc);
        if (optComment.isPresent()) {
            if (optComment.get().getTag().equals(CommentTag.APPROVAL)) {
                return PublicationContextState.APPROVED;
            }
            return PublicationContextState.REJECTED;
        }
        if (Objects.nonNull(current)) {
            if (userApprovedEverythingPossibleButNotAll(pc, current)) {
                return PublicationContextState.WAITING_FOR_OTHERS;
            }
            if (isApprovable(pc, current)) {
                return PublicationContextState.APPROVABLE;
            }
        }
        return PublicationContextState.CREATED;
    }

    private boolean userApprovedEverythingPossibleButNotAll(PublicationContext pc, User user) {
        List<Change> allInPublicationContextRelevantToUser =
            changeService.findAllInPublicationContextRelevantToUser(pc.getUri(), user.getUri());
        if (allInPublicationContextRelevantToUser.size() == pc.getChanges().size()) {
            return false;
        }
        return allInPublicationContextRelevantToUser.stream().allMatch(change -> change.getApprovedBy().contains(user));
    }

    private boolean isApprovable(PublicationContext pc, User user) {
        boolean userReviewedAtLeastOneWholeContext = false;
        Map<AbstractChangeableContext, List<Change>> contextChangesMap = new HashMap<>();
        for (Change change : pc.getChanges()) {
            if (change.notApproved()) {
                return false;
            }
            AbstractChangeableContext context = change.getContext();
            if (!contextChangesMap.containsKey(context)) {
                contextChangesMap.put(context, new ArrayList<>());
            }
            contextChangesMap.get(context).add(change);
        }
        for (List<Change> contextChanges : contextChangesMap.values()) {
            Set<User> approvedBy = new HashSet<>(contextChanges.get(0).getApprovedBy());
            if (approvedBy.contains(user)) {
                approvedBy.remove(user);
                if (contextChanges.stream().allMatch(change -> change.isApproved(user))) {
                    userReviewedAtLeastOneWholeContext = true;
                    continue;
                }
            }
            if (approvedBy.stream().noneMatch(
                approvingUser -> contextChanges.stream().allMatch(change -> change.isApproved(approvingUser)))) {
                return false;
            }
        }
        return userReviewedAtLeastOneWholeContext;
    }

    private Set<Change> takeIntoConsiderationExistingChanges(List<Change> currentChanges, Set<Change> existingChanges) {
        if (existingChanges.isEmpty()) {
            return new HashSet<>(currentChanges);
        }

        List<Change> newFormOfChanges = new ArrayList<>();
        for (Change currentChange : currentChanges) {
            Optional<Change> optExistingChange =
                existingChanges.stream().filter(currentChange::hasSameTripleAs).findFirst();
            if (optExistingChange.isEmpty()) {
                newFormOfChanges.add(currentChange);
                continue;
            }

            Change existingChange = optExistingChange.get();
            existingChanges.remove(existingChange);
            if (currentChange.hasSameChangeAs(existingChange)) {
                existingChange.setLabel(currentChange.getLabel());
                newFormOfChanges.add(existingChange);
            } else {
                newFormOfChanges.add(currentChange);
                changeService.remove(existingChange);
            }
        }

        for (Change rollbackedChange : existingChanges) {
            if (rollbackedChange.hasBeenReviewed()) {
                rollbackedChange.setChangeType(ChangeType.ROLLBACKED);
                rollbackedChange.clearReviews();
                newFormOfChanges.add(rollbackedChange);
            } else {
                changeService.remove(rollbackedChange);
            }
        }

        return new HashSet<>(newFormOfChanges);
    }

    private PublicationContext findRequiredFromProject(ProjectContext projectContext) {
        return publicationContextDao.findByProject(projectContext).orElseThrow(
            () -> new NotFoundException("Publication context related to project \"%s\" was not found.",
                projectContext.getUri()));
    }
}
