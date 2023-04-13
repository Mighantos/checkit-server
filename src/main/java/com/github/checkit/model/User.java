package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.Inferred;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@OWLClass(iri = TermVocabulary.s_c_uzivatel)
public class User extends AbstractUser {

    @Inferred
    @OWLObjectProperty(iri = TermVocabulary.s_p_je_gestorem)
    private Set<URI> gestoredVocabularies = new HashSet<>();

    public String toSimpleString() {
        return String.format("{%s,  \"%s\"}", getFullName(), getUri());
    }
}
