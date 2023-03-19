package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.PrePersist;
import cz.cvut.kbss.jopa.model.annotations.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_publikacni_kontext)
public class PublicationContext extends AbstractEntity {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_z_projektu)
    private ProjectContext fromProject;

    @OWLDataProperty(iri = TermVocabulary.s_p_ma_datum_a_cas_vytvoreni)
    private Instant created;

    @OWLDataProperty(iri = TermVocabulary.s_p_ma_datum_a_cas_posledni_modifikace)
    private Instant modified;

    @OWLObjectProperty(iri = TermVocabulary.s_p_ma_zmenu, cascade = CascadeType.ALL)
    private Set<Change> changes = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        preUpdate();
    }

    @PreUpdate
    public void preUpdate() {
        this.modified = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }
}
