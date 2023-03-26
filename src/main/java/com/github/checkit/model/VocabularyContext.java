package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_slovnikovy_kontext)
public class VocabularyContext extends AbstractChangeableContext {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_vychazi_z_verze, fetch = FetchType.EAGER)
    private Vocabulary basedOnVocabulary;

    @OWLObjectProperty(iri = TermVocabulary.s_p_odkazuje_na_prilohovy_kontext, fetch = FetchType.EAGER)
    private Set<AttachmentContext> attachmentContexts = new HashSet<>();
}
