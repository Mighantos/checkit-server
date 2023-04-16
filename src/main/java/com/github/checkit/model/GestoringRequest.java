package com.github.checkit.model;

import com.github.checkit.model.auxilary.AbstractEntity;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.PrePersist;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@NoArgsConstructor
@OWLClass(iri = TermVocabulary.s_c_pozadavek_na_gestorovani)
public class GestoringRequest extends AbstractEntity {

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_datum_a_cas_vytvoreni)
    private Instant created;

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_ma_zadatele, fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    private User applicant;

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_zada_o_gestorovani, fetch = FetchType.EAGER, cascade =
        CascadeType.DETACH)
    private Vocabulary vocabulary;

    public GestoringRequest(User applicant, Vocabulary vocabulary) {
        this.applicant = applicant;
        this.vocabulary = vocabulary;
    }

    @PrePersist
    public void prePersist() {
        this.created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    public String getId() {
        String uriString = getUri().toString();
        return uriString.substring(uriString.lastIndexOf("/") + 1);
    }

    @Override
    public String toString() {
        return "GestoringRequest{"
            + "uri=" + getUri()
            + ", created=" + created
            + ", applicant=" + (applicant != null ? applicant.getUri() : applicant)
            + ", vocabulary=" + (vocabulary != null ? vocabulary.getUri() : vocabulary)
            + '}';
    }
}
