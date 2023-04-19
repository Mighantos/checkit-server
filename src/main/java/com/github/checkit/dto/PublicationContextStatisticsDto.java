package com.github.checkit.dto;

import lombok.Getter;

@Getter
public class PublicationContextStatisticsDto {
    private final int totalChanges;
    private final int reviewableChanges;
    private final int approvedChanges;
    private final int rejectedChanges;

    /**
     * Constructor.
     */
    public PublicationContextStatisticsDto(int totalChanges, int reviewableChanges, int approvedChanges,
                                           int rejectedChanges) {
        this.totalChanges = totalChanges;
        this.reviewableChanges = reviewableChanges;
        this.approvedChanges = approvedChanges;
        this.rejectedChanges = rejectedChanges;
    }
}
