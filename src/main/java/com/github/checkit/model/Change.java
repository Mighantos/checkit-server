package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.EnumType;
import cz.cvut.kbss.jopa.model.annotations.Enumerated;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.vocabulary.RDF;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_zmena)
public class Change extends AbstractEntity {

    public Change(AbstractChangeableContext context) {
        this.context = context;
    }

    @OWLDataProperty(iri = RDFS.LABEL)
    private String label;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLAnnotationProperty(iri = RDF.SUBJECT)
    private URI subject;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLAnnotationProperty(iri = RDF.PREDICATE)
    private URI predicate;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLAnnotationProperty(iri = RDF.OBJECT)
    private String object;

    @OWLAnnotationProperty(iri = TermVocabulary.s_p_ma_novy_objekt)
    private String newObject;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @Enumerated(EnumType.OBJECT_ONE_OF)
    @OWLAnnotationProperty(iri = TermVocabulary.s_p_je_typu)
    private ChangeType changeType;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_v_kontextu)
    private AbstractChangeableContext context;

    @OWLObjectProperty(iri = TermVocabulary.s_p_schvaleno)
    private Set<User> approvedBy = new HashSet<>();

    @OWLObjectProperty(iri = TermVocabulary.s_p_zamitnuto)
    private Set<User> rejectedBy = new HashSet<>();

    public void addApprovedBy(User user) {
        rejectedBy.remove(user);
        approvedBy.add(user);
    }

    public void removeApprovedBy(User user) {
        approvedBy.remove(user);
    }

    public void addRejectedBy(User user) {
        approvedBy.remove(user);
        rejectedBy.add(user);
    }

    public void removeRejectedBy(User user) {
        rejectedBy.remove(user);
    }

    /**
     * Returns if {@link Change} is base on same triple as specified {@link Change}.
     *
     * @param right {@link Change} to compare to
     * @return if {@link Change} has same base triple
     */
    public boolean hasSameTripleAs(Change right) {
        Objects.requireNonNull(right);
        return this.subject.equals(right.subject) && this.predicate.equals(right.predicate)
            && this.object.equals(right.object);
    }

    /**
     * Returns if {@link Change} has the same changes as specified {@link Change}.
     *
     * @param right {@link Change} to compare to
     * @return if {@link Change} has the same changes
     */
    public boolean hasSameChangeAs(Change right) {
        Objects.requireNonNull(right);
        return hasSameTripleAs(right) && ((Objects.isNull(this.newObject) && Objects.isNull(right.newObject))
            || this.newObject.equals(right.newObject));
    }
}
