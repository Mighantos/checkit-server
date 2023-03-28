package com.github.checkit.dto;

import com.github.checkit.dto.auxiliary.PublicationContextState;
import com.github.checkit.model.PublicationContext;
import java.net.URI;
import lombok.Getter;

@Getter
public class PublicationContextDto {

    private final String id;
    private final URI uri;
    private final String label;
    private final URI projectContext;
    private final PublicationContextState state;

    /**
     * Constructor.
     */
    public PublicationContextDto(PublicationContext publicationContext, PublicationContextState state) {
        this.id = publicationContext.getId();
        this.uri = publicationContext.getUri();
        this.label = publicationContext.getFromProject().getLabel();
        this.projectContext = publicationContext.getFromProject().getUri();
        this.state = state;
    }
}
