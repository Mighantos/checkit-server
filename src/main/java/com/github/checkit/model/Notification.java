package com.github.checkit.model;

import com.github.checkit.model.auxilary.AbstractEntity;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.PrePersist;
import cz.cvut.kbss.jopa.vocabulary.DC;
import jakarta.validation.constraints.NotBlank;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_notifikace)
public class Notification extends AbstractEntity {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = DC.Terms.TITLE)
    private String title;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_content)
    private String content;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_about)
    private URL about;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_datum_a_cas_vytvoreni)
    private Instant created;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_addressed_to)
    private User addressedTo;

    @OWLDataProperty(iri = TermVocabulary.s_p_read_at)
    private Instant readAt;

    @PrePersist
    public void prePersist() {
        this.created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }
}
