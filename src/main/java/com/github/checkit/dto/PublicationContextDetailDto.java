package com.github.checkit.dto;

import com.github.checkit.dto.auxiliary.PublicationContextState;
import com.github.checkit.model.PublicationContext;
import java.util.List;
import lombok.Getter;

@Getter
public class PublicationContextDetailDto extends PublicationContextDto {

    private final List<ReviewableVocabularyDto> affectedVocabularies;

    /**
     * Constructor.
     */
    public PublicationContextDetailDto(PublicationContext publicationContext, PublicationContextState state,
                                       List<ReviewableVocabularyDto> affectedVocabularies) {
        super(publicationContext, state);
        this.affectedVocabularies = affectedVocabularies;
    }
}
