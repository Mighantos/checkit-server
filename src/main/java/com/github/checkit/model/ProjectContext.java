package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_metadatovy_kontext)
public class ProjectContext extends AbstractEntity {

    @OWLObjectProperty(iri = TermVocabulary.s_p_odkazuje_na_kontext, fetch = FetchType.EAGER)
    private Set<VocabularyContext> vocabularyContexts;
}
