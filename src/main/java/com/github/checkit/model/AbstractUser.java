package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.net.URI;
import java.util.Set;

@Data
@MappedSuperclass
public abstract class AbstractUser implements HasIdentifier, HasTypes, Serializable {
    @Id
    protected URI uri;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_krestni_jmeno)
    protected String firstName;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_prijmeni)
    protected String lastName;

    @Types
    protected Set<String> types;

    public String getId() {
        return uri != null
                ? uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1)
                : null;
    }
}
