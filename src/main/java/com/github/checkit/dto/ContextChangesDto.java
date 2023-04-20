package com.github.checkit.dto;

import com.github.checkit.dto.auxiliary.PublicationContextState;
import com.github.checkit.model.PublicationContext;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import lombok.Getter;

@Getter
public class ContextChangesDto {

    private final URI uri;
    private final String label;
    private final boolean gestored;
    private final String publicationId;
    private final String publicationLabel;
    private final Instant publicationLastUpdate;
    private final PublicationContextState publicationState;
    private final List<ChangeDto> changes;

    /**
     * Constructor.
     */
    public ContextChangesDto(URI uri, String label, boolean gestored, PublicationContext pc,
                             PublicationContextState publicationState, List<ChangeDto> changes) {
        this.uri = uri;
        this.label = label;
        this.gestored = gestored;
        this.publicationId = pc.getId();
        this.publicationLabel = pc.getFromProject().getLabel();
        this.publicationLastUpdate = pc.getModified();
        this.publicationState = publicationState;
        this.changes = changes;
    }
}
