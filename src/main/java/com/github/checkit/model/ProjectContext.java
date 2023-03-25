package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.vocabulary.DC;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_metadatovy_kontext)
public class ProjectContext extends AbstractEntity {

    @OWLDataProperty(iri = DC.Terms.TITLE)
    private String label;

    @OWLObjectProperty(iri = TermVocabulary.s_p_odkazuje_na_kontext, fetch = FetchType.EAGER)
    private Set<VocabularyContext> vocabularyContexts;
}
