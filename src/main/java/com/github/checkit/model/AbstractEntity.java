package com.github.checkit.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import java.io.Serializable;
import java.net.URI;
import java.util.Set;

import cz.cvut.kbss.jopa.model.annotations.Types;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@MappedSuperclass
@EqualsAndHashCode
public abstract class AbstractEntity implements HasIdentifier, HasTypes, Serializable {

    @Id(generated = true)
    private URI uri;

    @Types
    private Set<String> types;
}
