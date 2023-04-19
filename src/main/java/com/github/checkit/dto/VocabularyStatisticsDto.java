package com.github.checkit.dto;

import lombok.Getter;

@Getter
public class VocabularyStatisticsDto {

    private final int totalChanges;
    private final int approvedChanges;
    private final int rejectedChanges;

    /**
     * Constructor.
     */
    public VocabularyStatisticsDto(int totalChanges, int approvedChanges, int rejectedChanges) {
        this.totalChanges = totalChanges;
        this.approvedChanges = approvedChanges;
        this.rejectedChanges = rejectedChanges;
    }
}
