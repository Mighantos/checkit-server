package com.github.checkit.dto;

import com.github.checkit.model.Vocabulary;
import java.net.URI;
import java.util.List;
import lombok.Getter;

@Getter
public class VocabularyDto implements Comparable<VocabularyDto> {

    private final URI uri;
    private final String label;
    private final List<UserDto> gestors;

    /**
     * Constructor.
     */
    public VocabularyDto(Vocabulary vocabulary) {
        this.uri = vocabulary.getUri();
        this.label = vocabulary.getLabel();
        this.gestors = vocabulary.getGestors().stream().map(UserDto::new).toList();
    }

    @Override
    public int compareTo(VocabularyDto right) {
        return getLabel().toLowerCase().compareTo(right.getLabel().toLowerCase());
    }
}
