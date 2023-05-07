package com.github.checkit.model;

import com.github.checkit.model.auxilary.AbstractEntity;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.vocabulary.DC;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_metadatovy_kontext)
public class ProjectContext extends AbstractEntity {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = DC.Terms.TITLE)
    private String label;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_ma_autora)
    private User author;

    @OWLObjectProperty(iri = TermVocabulary.s_p_odkazuje_na_kontext, fetch = FetchType.EAGER)
    private Set<VocabularyContext> vocabularyContexts;

    public String getId() {
        return getUri().toString().substring(getUri().toString().lastIndexOf("/") + 1);
    }
}
