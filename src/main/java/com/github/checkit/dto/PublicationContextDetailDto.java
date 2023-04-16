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
                                       CommentDto finalComment, List<ReviewableVocabularyDto> affectedVocabularies) {
        super(publicationContext, state, finalComment);
        this.affectedVocabularies = affectedVocabularies;
    }
}
