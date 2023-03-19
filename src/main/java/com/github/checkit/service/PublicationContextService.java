package com.github.checkit.service;

import com.github.checkit.dao.BaseDao;
import com.github.checkit.dao.PublicationContextDao;
import com.github.checkit.exception.NotFoundException;
import com.github.checkit.model.Change;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.VocabularyContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.jena.atlas.lib.NotImplemented;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicationContextService extends BaseRepositoryService<PublicationContext> {

    private final PublicationContextDao publicationContextDao;
    private final ProjectContextService projectContextService;
    private final ChangeService changeService;

    /**
     * Construct.
     */
    public PublicationContextService(PublicationContextDao publicationContextDao,
                                     ProjectContextService projectContextService, ChangeService changeService) {
        this.publicationContextDao = publicationContextDao;
        this.projectContextService = projectContextService;
        this.changeService = changeService;
    }

    @Override
    protected BaseDao<PublicationContext> getPrimaryDao() {
        return publicationContextDao;
    }


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

    private Set<Change> takeIntoConsiderationExistingChanges(List<Change> currentChanges, Set<Change> existingChanges) {
        List<Change> newFormOfChanges = new ArrayList<>();
        if (existingChanges.isEmpty()) {
            return new HashSet<>(currentChanges);
        }
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
                continue;
            }
            throw new NotImplemented();
        }

        return new HashSet<>(newFormOfChanges);
    }

    private PublicationContext findRequiredFromProject(ProjectContext projectContext) {
        return publicationContextDao.find(projectContext).orElseThrow(
            () -> new NotFoundException("Publication context related to project \"%s\" was not found.",
                projectContext.getUri()));
    }
}
