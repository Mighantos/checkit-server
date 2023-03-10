package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@OWLClass(iri = TermVocabulary.s_c_pozadavek_na_gestorovani)
public class GestoringRequest extends AbstractEntity {

    //    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_datum_a_cas_vytvoreni)
    private Instant created;

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_ma_zadatele, fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    private User applicant;

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_ma_zada_o_gestorovani, fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    private Vocabulary vocabulary;

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
        return "GestoringRequest{" +
                "uri=" + getUri() +
                ", created=" + created +
                ", applicant=" + (applicant != null ? applicant.getUri() : applicant) +
                ", vocabulary=" + (vocabulary != null ? vocabulary.getUri() : vocabulary) +
                '}';
    }
}
