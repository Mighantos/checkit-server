package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.model.ChangeObject;
import java.net.URI;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ChangeObjectDto {

    private final String value;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final URI type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String languageTag;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final RelationshipDto restriction;
    @JsonIgnore
    private final boolean blankNode;

    /**
     * Constructor.
     */
    public ChangeObjectDto(ChangeObject objectResource) {
        this.value = objectResource.getValue();
        this.type = objectResource.getType();
        this.languageTag = objectResource.getLanguage();
        this.restriction = null;
        this.blankNode = objectResource.isBlankNode();
    }

    /**
     * Constructor.
     */
    public ChangeObjectDto(RelationshipDto restriction) {
        this.value = null;
        this.type = null;
        this.languageTag = null;
        this.restriction = restriction;
        this.blankNode = false;
    }
}
