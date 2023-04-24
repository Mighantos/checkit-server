package com.github.checkit.model;

import com.github.checkit.model.auxilary.AbstractChangeableContext;
import com.github.checkit.model.auxilary.AbstractCommentableEntity;
import com.github.checkit.model.auxilary.ChangeSubjectType;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.EnumType;
import cz.cvut.kbss.jopa.model.annotations.Enumerated;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
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
public class Change extends AbstractCommentableEntity {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @Enumerated(EnumType.OBJECT_ONE_OF)
    @OWLAnnotationProperty(iri = TermVocabulary.s_p_je_typu)
    private ChangeType changeType;

    @OWLDataProperty(iri = RDFS.LABEL)
    private MultilingualString label;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_je_pocitatelna_do_statistiky)
    private Boolean countable;

    @Enumerated
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_popis_typu_subjektu)
    private ChangeSubjectType subjectType;

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
    @OWLObjectProperty(iri = RDF.OBJECT, cascade = CascadeType.ALL)
    private ChangeObject object;

    @OWLObjectProperty(iri = TermVocabulary.s_p_ma_novy_objekt, cascade = CascadeType.ALL)
    private ChangeObject newObject;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_v_kontextu)
    private AbstractChangeableContext context;

    @OWLObjectProperty(iri = TermVocabulary.s_p_schvaleno, fetch = FetchType.EAGER)
    private Set<User> approvedBy;

    @OWLObjectProperty(iri = TermVocabulary.s_p_zamitnuto, fetch = FetchType.EAGER)
    private Set<User> rejectedBy;

    public Change(AbstractChangeableContext context) {
        this.context = context;
    }

    public String getId() {
        return getUri().toString().substring(getUri().toString().lastIndexOf("/") + 1);
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public Set<User> getApprovedBy() {
        if (Objects.isNull(approvedBy)) {
            approvedBy = new HashSet<>();
        }
        return approvedBy;
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public Set<User> getRejectedBy() {
        if (Objects.isNull(rejectedBy)) {
            rejectedBy = new HashSet<>();
        }
        return rejectedBy;
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public MultilingualString getLabel() {
        if (Objects.isNull(label)) {
            label = new MultilingualString();
        }
        return label;
    }

    public void addApprovedBy(User user) {
        getApprovedBy().add(user);
    }

    public void removeApprovedBy(User user) {
        getApprovedBy().remove(user);
    }

    public void addRejectedBy(User user) {
        getRejectedBy().add(user);
    }

    public void removeRejectedBy(User user) {
        getRejectedBy().remove(user);
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

    public boolean hasBeenReviewed() {
        return !getApprovedBy().isEmpty() || !getRejectedBy().isEmpty();
    }

    public void clearReviews() {
        getApprovedBy().clear();
        getRejectedBy().clear();
    }

    public boolean isApproved(User user) {
        return getApprovedBy().contains(user);
    }

    public boolean isRejected(User user) {
        return getRejectedBy().contains(user);
    }

    public boolean notApproved() {
        return getApprovedBy().isEmpty();
    }
}
