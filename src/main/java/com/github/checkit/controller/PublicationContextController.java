package com.github.checkit.controller;

import com.github.checkit.service.PublicationContextService;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
