package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardinalityDto {

    private Integer min;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer max = null;
}
