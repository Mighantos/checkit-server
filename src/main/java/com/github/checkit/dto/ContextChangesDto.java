package com.github.checkit.dto;

import java.net.URI;
import java.util.List;
import lombok.Getter;

@Getter
public class ContextChangesDto {

    private final URI uri;
    private final String label;
    private final List<ChangeDto> changes;

    /**
     * Constructor.
     */
    public ContextChangesDto(URI uri, String label, List<ChangeDto> changes) {
        this.uri = uri;
        this.label = label;
        this.changes = changes;
    }
}
