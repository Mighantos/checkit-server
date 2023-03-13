package com.github.checkit.dto;

import lombok.Getter;

@Getter
public class AdminPanelSummaryDto {
    private final int pendingGestoringRequestCount;
    private final int vocabularyWithGestorCount;
    private final int vocabularyCount;
    private final int adminCount;

    /**
     * Constructor.
     */
    public AdminPanelSummaryDto(int pendingGestoringRequestCount, int vocabularyWithGestorCount, int vocabularyCount,
                                int adminCount) {
        this.pendingGestoringRequestCount = pendingGestoringRequestCount;
        this.vocabularyWithGestorCount = vocabularyWithGestorCount;
        this.vocabularyCount = vocabularyCount;
        this.adminCount = adminCount;
    }
}
