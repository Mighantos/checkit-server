package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.PublicationContextDao;
import com.github.checkit.dto.PublicationContextDto;
import com.github.checkit.dto.VocabularyDto;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.util.PublicationContextState;
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
    private final UserService userService;

    /**
     * Construct.
     */
    public PublicationContextService(PublicationContextDao publicationContextDao,
                                     ProjectContextService projectContextService, ChangeService changeService,
                                     UserService userService) {
        this.publicationContextDao = publicationContextDao;
        this.projectContextService = projectContextService;
        this.changeService = changeService;
        this.userService = userService;
    }

    @Override
    protected BaseDao<PublicationContext> getPrimaryDao() {
        return publicationContextDao;
    }

    /**
     * Gets list of publication contexts relevant to current user.
     *
     * @return list of publication contexts
     */
    @Transactional
    public List<PublicationContextDto> getRelevantPublicationContexts() {
        URI userUri = userService.getCurrent().getUri();
        List<PublicationContext> publicationContexts =
            publicationContextDao.findAllThatAffectVocabularies(userUri);
        return publicationContexts.stream().map(pc -> {
            List<VocabularyDto> affectedVocabularies =
                publicationContextDao.findAffectedVocabularies(pc.getUri()).stream().map(VocabularyDto::new).toList();
            PublicationContextState state = getState(pc);
            return new PublicationContextDto(pc, affectedVocabularies, state);
        }).toList();
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
        return findRequired(publicationContextDao.find(projectContext).orElseThrow(
            () -> new NotFoundException("Publication context related to project \"%s\" was not found.",
                projectContext.getUri())));
    }
}
