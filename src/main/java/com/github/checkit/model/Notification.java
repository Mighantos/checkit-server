package com.github.checkit.model;

import com.github.checkit.model.auxilary.AbstractEntity;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.PrePersist;
import cz.cvut.kbss.jopa.vocabulary.DC;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_notifikace)
public class Notification extends AbstractEntity {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = DC.Terms.TITLE)
    private MultilingualString title;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_content)
    private MultilingualString content;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_about)
    private String about;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_datum_a_cas_vytvoreni)
    private Instant created;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = TermVocabulary.s_p_addressed_to)
    private User addressedTo;

    @OWLDataProperty(iri = TermVocabulary.s_p_read_at)
    private Instant readAt;

    @PrePersist
    public void prePersist() {
        this.created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    /**
     * Creates a copy using specified notification as template (only deep coping title, content and about).
     *
     * @param template notification
     */
    public static Notification createFromTemplate(Notification template) {
        Notification notification = new Notification();
        notification.setTitle(new MultilingualString(template.getTitle().getValue()));
        notification.setContent(new MultilingualString(template.getContent().getValue()));
        notification.setAbout(template.getAbout());
        return notification;
    }

    public void markRead() {
        setReadAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }
}
