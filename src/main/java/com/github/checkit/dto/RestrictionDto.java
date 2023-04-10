package com.github.checkit.dto;

import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestrictionDto {

    private List<ChangeDto> changes;
    private String startName;
    private URI startUri;
    private CardinalityDto cardinalityStart;
    private String endName;
    private URI endUri;
    private CardinalityDto cardinalityEnd;
    private String relationName;
    private URI relationUri;

    public RestrictionDto() {
        this.cardinalityStart = new CardinalityDto();
        this.cardinalityEnd = new CardinalityDto();
    }
}
