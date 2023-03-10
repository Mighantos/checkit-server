package com.github.checkit.dto;

import com.github.checkit.model.GestoringRequest;
import java.net.URI;
import java.time.Instant;
import lombok.Getter;

@Getter
public class GestoringRequestDto {

    private final String id;
    private final URI uri;
    private final Instant created;
    private final URI applicant;
    private final URI vocabulary;

    /**
     * Constructor.
     */
    public GestoringRequestDto(GestoringRequest gestoringRequest) {
        this.id = gestoringRequest.getId();
        this.uri = gestoringRequest.getUri();
        this.created = gestoringRequest.getCreated();
        this.applicant = gestoringRequest.getApplicant().getUri();
        this.vocabulary = gestoringRequest.getVocabulary().getUri();
    }
}
