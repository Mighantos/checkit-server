package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.model.Vocabulary;
import java.net.URI;
import java.util.List;
import lombok.Getter;

@Getter
public class ReviewableVocabularyDto {

    private final URI uri;
    private final String label;
    private final boolean gestored;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final VocabularyStatisticsDto statistics;
    private final List<UserDto> gestors;

    /**
     * Constructor.
     */
    public ReviewableVocabularyDto(Vocabulary vocabulary, boolean gestored, VocabularyStatisticsDto statistics) {
        this.uri = vocabulary.getUri();
        this.label = vocabulary.getLabel();
        this.gestored = gestored;
        this.gestors = vocabulary.getGestors().stream().map(UserDto::new).toList();
        this.statistics = statistics;
    }
}
