package com.github.checkit.dto;

import com.github.checkit.model.Vocabulary;
import java.net.URI;
import lombok.Getter;

@Getter
public class ReviewableVocabularyDto {

    private final URI uri;
    private final String label;
    private final boolean gestored;

    /**
     * Constructor.
     */
    public ReviewableVocabularyDto(Vocabulary vocabulary, boolean gestored) {
        this.uri = vocabulary.getUri();
        this.label = vocabulary.getLabel();
        this.gestored = gestored;
    }
}
