package com.github.checkit.dto;

import com.github.checkit.model.PublicationContext;
import com.github.checkit.util.PublicationContextState;
import java.net.URI;
import java.util.List;
import lombok.Getter;

@Getter
public class PublicationContextDto {
    private final String id;
    private final URI uri;
    private final String label;
    private final URI projectContext;
    private final List<VocabularyDto> affectedVocabularies;
    private final PublicationContextState state;

    /**
     * Constructor.
     */
    public PublicationContextDto(PublicationContext publicationContext, List<VocabularyDto> affectedVocabularies,
                                 PublicationContextState state) {
        this.id = publicationContext.getId();
        this.uri = publicationContext.getUri();
        this.label = publicationContext.getFromProject().getLabel();
        this.projectContext = publicationContext.getFromProject().getUri();
        this.affectedVocabularies = affectedVocabularies;
        this.state = state;
    }
}
