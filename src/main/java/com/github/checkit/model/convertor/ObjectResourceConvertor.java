package com.github.checkit.model.convertor;

import com.github.checkit.model.ObjectResource;
import cz.cvut.kbss.jopa.model.AttributeConverter;
import cz.cvut.kbss.jopa.model.annotations.Converter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Converter
public class ObjectResourceConvertor implements AttributeConverter<ObjectResource, String> {

    @Override
    public String convertToAxiomValue(ObjectResource value) {
        return value.toAxiom();
    }

    @Override
    public ObjectResource convertToAttribute(String value) {
        return ObjectResource.of(value);
    }
}
