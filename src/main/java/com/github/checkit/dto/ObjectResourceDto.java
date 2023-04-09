package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.model.ObjectResource;
import java.net.URI;
import java.util.Objects;
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

    /**
     * Constructor.
     */
    public ObjectResourceDto(ObjectResource objectResource) {
        this.value = objectResource.getValue();
        this.type = objectResource.getType();
        this.languageTag = objectResource.getLanguage();
        this.restriction = null;
    }

    /**
     * Constructor.
     */
    public ObjectResourceDto(RestrictionDto restriction) {
        this.value = null;
        this.type = null;
        this.languageTag = null;
        this.restriction = restriction;
    }

    @JsonIgnore
    public boolean isBlankNode() {
        return (Objects.isNull(value) || value.isEmpty()) && Objects.isNull(type) && Objects.isNull(languageTag);
    }
}
