package com.github.checkit.dto;

import com.github.checkit.model.Vocabulary;
import java.net.URI;
import lombok.Getter;

@Getter
public class VocabularyInfoDto implements Comparable<VocabularyInfoDto> {

    private final URI uri;
    private final String label;

    /**
     * Constructor.
     */
    public VocabularyInfoDto(Vocabulary vocabulary) {
        this.uri = vocabulary.getUri();
        this.label = vocabulary.getLabel();
    }

    @Override
    public int compareTo(VocabularyInfoDto right) {
        return getLabel().toLowerCase().compareTo(right.getLabel().toLowerCase());
    }
}
