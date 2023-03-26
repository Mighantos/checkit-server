package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_prilohovy_kontext)
public class AttachmentContext extends AbstractChangeableContext {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_vychazi_z_verze, fetch = FetchType.EAGER)
    private URI basedOnAttachment;
}
