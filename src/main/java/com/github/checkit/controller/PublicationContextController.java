package com.github.checkit.controller;

import com.github.checkit.config.properties.RepositoryConfigProperties;
import com.github.checkit.dto.ContextChangesDto;
import com.github.checkit.dto.PublicationContextDetailDto;
import com.github.checkit.dto.PublicationContextDto;
import com.github.checkit.service.PublicationContextService;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PublicationContextController.MAPPING)
public class PublicationContextController extends BaseController {

    public static final String MAPPING = "/publication-contexts";
    private final PublicationContextService publicationContextService;
    private final RepositoryConfigProperties repositoryConfigProperties;

    public PublicationContextController(PublicationContextService publicationContextService,
                                        RepositoryConfigProperties repositoryConfigProperties) {
        this.publicationContextService = publicationContextService;
        this.repositoryConfigProperties = repositoryConfigProperties;
    }

    @GetMapping("/readonly")
    public List<PublicationContextDto> getReadonlyPublicationContexts() {
        return publicationContextService.getReadonlyPublicationContexts();
    }

    @GetMapping("/reviewable")
    public List<PublicationContextDto> getReviewablePublicationContexts() {
        return publicationContextService.getReviewablePublicationContexts();
    }

    @GetMapping("/closed")
    public List<PublicationContextDto> getClosedPublicationContexts(@RequestParam(required = false, defaultValue = "0")
                                                                    int pageNumber) {
        return publicationContextService.getClosedPublicationContexts(pageNumber);
    }

    @GetMapping("/closed/page-count")
    public int getPageCountOfClosedPublicationContexts() {
        return publicationContextService.getPageCountOfClosedPublicationContexts();
    }

    @GetMapping("/{publicationContextId}")
    public PublicationContextDetailDto getPublicationContextDetail(@PathVariable String publicationContextId) {
        return publicationContextService.getPublicationContextDetail(publicationContextId);
    }

    /**
     * Get changes of specified vocabulary in specified publication context with labels in preferred language.
     *
     * @param publicationContextId identifier of publication context
     * @param vocabularyUri        URI identifier of vocabulary
     * @param language             preferred language tag
     * @return Object with name of vocabulary a list of changes
     */
    @GetMapping("/{publicationContextId}/vocabulary-changes")
    public ContextChangesDto getPublicationContextDetail(@PathVariable String publicationContextId,
                                                         @RequestParam("vocabularyUri") URI vocabularyUri,
                                                         @RequestParam(required = false) String language) {
        if (Objects.isNull(language) || language.isEmpty()) {
            language = repositoryConfigProperties.getLanguage();
        }
        return publicationContextService.getChangesInContextInPublicationContext(publicationContextId, vocabularyUri,
            language);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public URI submitProjectForReview(@RequestBody URI projectUri) {
        return publicationContextService.createOrUpdatePublicationContext(projectUri);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{publicationContextId}/approved")
    public void approvePublicationContext(@PathVariable String publicationContextId,
                                          @RequestBody String finalComment) {
        publicationContextService.approvePublicationContext(publicationContextId, finalComment);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{publicationContextId}/rejected")
    public void rejectPublicationContext(@PathVariable String publicationContextId,
                                         @RequestBody String finalComment) {
        publicationContextService.rejectPublicationContext(publicationContextId, finalComment);
    }
}
