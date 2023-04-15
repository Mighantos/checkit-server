package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.model.ObjectResource;
import java.net.URI;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ObjectResourceDto {

    private final String value;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final URI type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String languageTag;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final RestrictionDto restriction;
    @JsonIgnore
    private final boolean blankNode;

    /**
     * Constructor.
     */
    public ObjectResourceDto(ObjectResource objectResource) {
        this.value = objectResource.getValue();
        this.type = objectResource.getType();
        this.languageTag = objectResource.getLanguage();
        this.restriction = null;
        this.blankNode = objectResource.getBlankNode();
    }

    /**
     * Constructor.
     */
    public ObjectResourceDto(RestrictionDto restriction) {
        this.value = null;
        this.type = null;
        this.languageTag = null;
        this.restriction = restriction;
        this.blankNode = false;
    }
}
