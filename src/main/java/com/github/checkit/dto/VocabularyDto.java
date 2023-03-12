package com.github.checkit.dto;

import com.github.checkit.model.Vocabulary;
import java.util.List;
import lombok.Getter;

@Getter
public class VocabularyDto extends VocabularyInfoDto {
    private final List<UserDto> gestors;

    /**
     * Constructor.
     */
    public VocabularyDto(Vocabulary vocabulary) {
        super(vocabulary);
        this.gestors = vocabulary.getGestors().stream().map(UserDto::new).toList();
    }
}
