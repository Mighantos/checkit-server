package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@OWLClass(iri = TermVocabulary.s_c_uzivatel)
public class User extends AbstractUser {

    @Transient
    private Set<URI> gestoredVocabularies = new HashSet<>();
}
