package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import lombok.EqualsAndHashCode;

@OWLClass(iri = TermVocabulary.s_c_uzivatel)
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractUser {
}
