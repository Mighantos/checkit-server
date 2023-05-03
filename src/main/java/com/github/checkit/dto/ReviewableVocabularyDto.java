package com.github.checkit.dto;

import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import java.net.URI;
import java.util.List;
import lombok.Getter;

@Getter
public class ReviewableVocabularyDto {

    private final URI uri;
    private final String label;
    private final boolean gestored;
    private final VocabularyStatisticsDto statistics;
    private final List<UserDto> gestors;
    private final List<UserDto> approvedByUsers;

    /**
     * Constructor.
     */
    public ReviewableVocabularyDto(Vocabulary vocabulary, boolean gestored, VocabularyStatisticsDto statistics,
                                   List<User> approvedBy) {
        this.uri = vocabulary.getUri();
        this.label = vocabulary.getLabel();
        this.gestored = gestored;
        this.gestors = vocabulary.getGestors().stream().map(UserDto::new).toList();
        this.approvedByUsers = approvedBy.stream().map(UserDto::new).toList();
        this.statistics = statistics;
    }
}
