package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.model.ObjectResource;
import java.net.URI;
import lombok.Getter;

@Getter
public class ObjectResourceDto {

    private final String value;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final URI type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String languageTag;

    /**
     * Constructor.
     */
    public ObjectResourceDto(ObjectResource objectResource) {
        this.value = objectResource.getValue();
        this.type = objectResource.getType();
        this.languageTag = objectResource.getLanguage();
    }
}
