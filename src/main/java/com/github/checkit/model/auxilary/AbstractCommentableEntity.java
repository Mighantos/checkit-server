package com.github.checkit.model.auxilary;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

@MappedSuperclass
@OWLClass(iri = TermVocabulary.s_c_komentovatelna_entita)
public abstract class AbstractCommentableEntity extends AbstractEntity {
}
