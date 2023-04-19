package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final CommentDto finalComment;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PublicationContextStatisticsDto statistics;

    /**
     * Constructor.
     */
    public PublicationContextDto(PublicationContext publicationContext, PublicationContextState state,
                                 CommentDto finalComment) {
        this.id = publicationContext.getId();
        this.uri = publicationContext.getUri();
        this.label = publicationContext.getFromProject().getLabel();
        this.projectContext = publicationContext.getFromProject().getUri();
        this.state = state;
        this.finalComment = finalComment;
    }

    /**
     * Constructor.
     */
    public PublicationContextDto(PublicationContext publicationContext, PublicationContextState state,
                                 CommentDto finalComment,
                                 PublicationContextStatisticsDto statistics) {
        this(publicationContext, state, finalComment);
        this.statistics = statistics;
    }
}
