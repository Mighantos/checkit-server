package com.github.checkit.model;

import com.github.checkit.model.auxilary.AbstractEntity;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.PrePersist;
import cz.cvut.kbss.jopa.model.annotations.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_Comment)
public class Comment extends AbstractEntity implements Comparable<Comment> {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_topic)
    private Change topic;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_has_creator)
    private User author;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_datum_a_cas_vytvoreni)
    private Instant created;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_datum_a_cas_posledni_modifikace)
    private Instant modified;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_content)
    private String content;

    @PrePersist
    public void prePersist() {
        this.created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        preUpdate();
    }

    @PreUpdate
    public void preUpdate() {
        this.modified = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    @Override
    public int compareTo(Comment o) {
        return this.created.compareTo(o.created);
    }
}
