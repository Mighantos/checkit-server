package com.github.checkit.dto;

import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import lombok.Getter;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Getter
public class VocabularyDto {

    private final URI uri;
    private final String label;
    private final List<URI> gestors;

    public VocabularyDto(Vocabulary vocabulary) {
        this.uri = vocabulary.getUri();
        this.label = vocabulary.getLabel();
        this.gestors = extractUrisFromGestors(vocabulary.getGestors());
    }

    private List<URI> extractUrisFromGestors(Set<User> gestors) {
        return gestors.stream().map(User::getUri).toList();
    }
}
