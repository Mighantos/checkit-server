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

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping
    public void submitProjectForReview(@RequestBody URI projectUri) {
        publicationContextService.createOrUpdatePublicationContext(projectUri);
    }

    @GetMapping
    public List<PublicationContextDto> getRelevantPublicationContexts() {
        return publicationContextService.getRelevantPublicationContexts();
    }

    @GetMapping("/{id}")
    public PublicationContextDetailDto getPublicationContextDetail(@PathVariable String id) {
        return publicationContextService.getPublicationContextDetail(id);
    }

    @GetMapping("/{id}/vocabulary-changes")
    public ContextChangesDto getPublicationContextDetail(@PathVariable String id,
                                                         @RequestParam("vocabularyUri") URI vocabularyUri) {
        return publicationContextService.getChangesInContextInPublicationContext(id, vocabularyUri);
    }
}
