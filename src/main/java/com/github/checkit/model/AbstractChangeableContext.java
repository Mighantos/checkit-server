package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

@MappedSuperclass
@OWLClass(iri = TermVocabulary.s_c_kontext)
public abstract class AbstractChangeableContext extends AbstractEntity {
}
