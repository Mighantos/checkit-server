package com.github.checkit.controller;

import com.github.checkit.dto.ContextChangesDto;
import com.github.checkit.dto.PublicationContextDetailDto;
import com.github.checkit.dto.PublicationContextDto;
import com.github.checkit.service.PublicationContextService;
import java.net.URI;
import java.util.List;
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

    public PublicationContextController(PublicationContextService publicationContextService) {
        this.publicationContextService = publicationContextService;
    }

    @GetMapping("/readonly")
    public List<PublicationContextDto> getReadonlyPublicationContexts() {
        return publicationContextService.getReadonlyPublicationContexts();
    }

    @GetMapping("/reviewable")
    public List<PublicationContextDto> getReviewablePublicationContexts() {
        return publicationContextService.getReviewablePublicationContexts();
    }

    @GetMapping("/{publicationContextId}")
    public PublicationContextDetailDto getPublicationContextDetail(@PathVariable String publicationContextId) {
        return publicationContextService.getPublicationContextDetail(publicationContextId);
    }

    @GetMapping("/{publicationContextId}/vocabulary-changes")
    public ContextChangesDto getPublicationContextDetail(@PathVariable String publicationContextId,
                                                         @RequestParam("vocabularyUri") URI vocabularyUri) {
        return publicationContextService.getChangesInContextInPublicationContext(publicationContextId, vocabularyUri);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public URI submitProjectForReview(@RequestBody URI projectUri) {
        return publicationContextService.createOrUpdatePublicationContext(projectUri);
    }
}
