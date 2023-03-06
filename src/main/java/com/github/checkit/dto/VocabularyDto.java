package com.github.checkit.dto;

import com.github.checkit.model.Vocabulary;
import lombok.Getter;

import java.net.URI;
import java.util.List;

@Getter
public class VocabularyDto {

    private final URI uri;
    private final String label;
    private final List<UserDto> gestors;

    public VocabularyDto(Vocabulary vocabulary) {
        this.uri = vocabulary.getUri();
        this.label = vocabulary.getLabel();
        this.gestors = vocabulary.getGestors().stream().map(UserDto::new).toList();
    }
}
