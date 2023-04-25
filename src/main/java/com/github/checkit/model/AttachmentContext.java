package com.github.checkit.model;

import com.github.checkit.model.auxilary.AbstractChangeableContext;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_prilohovy_kontext)
public class AttachmentContext extends AbstractChangeableContext {
}
