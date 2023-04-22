package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
public class VocabularyStatisticsDto {

    private final int totalChanges;
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer approvedChanges;
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer rejectedChanges;

    /**
     * Constructor.
     */
    public VocabularyStatisticsDto(int totalChanges) {
        this.totalChanges = totalChanges;
    }
}
