package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class PublicationContextStatisticsDto {

    private final int totalChanges;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer reviewableChanges;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer approvedChanges;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer rejectedChanges;

    /**
     * Constructor.
     */
    public PublicationContextStatisticsDto(int totalChanges) {
        this.totalChanges = totalChanges;
        this.reviewableChanges = null;
        this.approvedChanges = null;
        this.rejectedChanges = null;
    }

    /**
     * Constructor.
     */
    public PublicationContextStatisticsDto(int totalChanges, Integer reviewableChanges, Integer approvedChanges,
                                           Integer rejectedChanges) {
        this.totalChanges = totalChanges;
        this.reviewableChanges = reviewableChanges;
        this.approvedChanges = approvedChanges;
        this.rejectedChanges = rejectedChanges;
    }
}
