package com.github.checkit.dto;

import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelationshipDto {

    private List<ChangeDto> affectedChanges;
    private URI commentableChange;
    private String startName;
    private URI startUri;
    private CardinalityDto cardinalityStart;
    private String endName;
    private URI endUri;
    private CardinalityDto cardinalityEnd;
    private String relationName;
    private URI relationUri;

    public RelationshipDto() {
        this.cardinalityStart = new CardinalityDto();
        this.cardinalityEnd = new CardinalityDto();
    }
}
