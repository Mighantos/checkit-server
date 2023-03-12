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
    private final UserDto applicant;
    private final VocabularyInfoDto vocabulary;

    /**
     * Constructor.
     */
    public GestoringRequestDto(GestoringRequest gestoringRequest) {
        this.id = gestoringRequest.getId();
        this.uri = gestoringRequest.getUri();
        this.created = gestoringRequest.getCreated();
        this.applicant = new UserDto(gestoringRequest.getApplicant());
        this.vocabulary = new VocabularyInfoDto(gestoringRequest.getVocabulary());
    }
}
