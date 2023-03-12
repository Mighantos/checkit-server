package com.github.checkit.dto;

import com.github.checkit.model.Vocabulary;
import java.net.URI;
import lombok.Getter;

@Getter
public class VocabularyInfoDto {

    private final URI uri;
    private final String label;

    /**
     * Constructor.
     */
    public VocabularyInfoDto(Vocabulary vocabulary) {
        this.uri = vocabulary.getUri();
        this.label = vocabulary.getLabel();
    }
}
