package com.github.checkit.model.auxilary;

import com.github.checkit.model.HasIdentifier;
import com.github.checkit.model.HasTypes;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.Types;
import java.io.Serializable;
import java.net.URI;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@MappedSuperclass
@EqualsAndHashCode
public abstract class AbstractEntity implements HasIdentifier, HasTypes, Serializable {

    @Id(generated = true)
    private URI uri;

    @Types
    private Set<String> types; // = new HashSet<>(); shows correct exception
}
