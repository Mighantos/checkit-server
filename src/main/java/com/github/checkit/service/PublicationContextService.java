package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.PublicationContextDao;
import com.github.checkit.dto.ChangeDto;
import com.github.checkit.dto.ContextChangesDto;
import com.github.checkit.dto.PublicationContextDetailDto;
import com.github.checkit.dto.PublicationContextDto;
import com.github.checkit.dto.ReviewableVocabularyDto;
import com.github.checkit.dto.auxiliary.PublicationContextState;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.util.TermVocabulary;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicationContextService extends BaseRepositoryService<PublicationContext> {

    private final PublicationContextDao publicationContextDao;
    private final ProjectContextService projectContextService;
    private final ChangeService changeService;
    private final VocabularyService vocabularyService;
    private final UserService userService;

    /**
     * Construct.
     */
    public PublicationContextService(PublicationContextDao publicationContextDao,
                                     ProjectContextService projectContextService, ChangeService changeService,
                                     VocabularyService vocabularyService, UserService userService) {
        this.publicationContextDao = publicationContextDao;
        this.projectContextService = projectContextService;
        this.changeService = changeService;
        this.vocabularyService = vocabularyService;
        this.userService = userService;
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
            PublicationContextState state = getState(pc);
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
        URI userUri = userService.getCurrent().getUri();
        List<PublicationContext> publicationContexts =
            publicationContextDao.findAllThatAffectVocabulariesGestoredBy(userUri);
        return publicationContexts.stream().map(pc -> {
            PublicationContextState state = getState(pc);
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
        PublicationContextState state = getState(pc);
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
     * @return basic information about context and changes made
     */
    @Transactional
    public ContextChangesDto getChangesInContextInPublicationContext(String publicationContextId, URI vocabularyUri) {
        User current = userService.getCurrent();
        URI publicationContextUri = createPublicationContextUriFromId(publicationContextId);
        boolean allowedToReview =
            publicationContextDao.doesUserHavePermissionToReviewVocabulary(current.getUri(), publicationContextUri,
                vocabularyUri);

        PublicationContext pc = findRequired(publicationContextUri);
        String vocabularyLabel = vocabularyService.findRequired(vocabularyUri).getLabel();
        List<ChangeDto> changes =
            pc.getChanges().stream().filter(change ->
                    ((VocabularyContext) change.getContext()).getBasedOnVocabulary().getUri().equals(vocabularyUri))
                .map(change -> new ChangeDto(change, current)).sorted().toList();
        return new ContextChangesDto(vocabularyUri, vocabularyLabel, allowedToReview, changes);
    }

    /**
     * Create (or update if already exists) publication context with all changes made in specified project compared to
     * canonical version of its vocabularies and attachments.
     *
     * @param projectUri project URI identifier
     */
    @Transactional
    public void createOrUpdatePublicationContext(URI projectUri) {
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
            publicationContext = new PublicationContext();
            publicationContext.setFromProject(project);
        }

        Set<Change> newFormOfChanges =
            takeIntoConsiderationExistingChanges(currentChanges, publicationContext.getChanges());
        publicationContext.setChanges(newFormOfChanges);

        if (publicationContextExists) {
            update(publicationContext);
        } else {
            persist(publicationContext);
        }
    }

    private URI createPublicationContextUriFromId(String id) {
        return URI.create(TermVocabulary.s_c_publikacni_kontext + "/" + id);
    }

    private PublicationContextState getState(PublicationContext pc) {
        List<Change> changes = new ArrayList<>(pc.getChanges());
        if (changes.stream().anyMatch(Change::isRejected)) {
            return PublicationContextState.REJECTED;
        }
        for (User user : changes.get(0).getApprovedBy()) {
            if (changes.stream().allMatch(change -> change.getApprovedBy().contains(user))) {
                return PublicationContextState.APPROVED;
            }
        }
        return PublicationContextState.CREATED;
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
