package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.Types;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class AbstractUser implements HasIdentifier, HasTypes, Serializable {
    @Id
    protected URI uri;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_krestni_jmeno)
    protected String firstName;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = TermVocabulary.s_p_ma_prijmeni)
    protected String lastName;

    @Types
    protected Set<String> types;

    /**
     * Gets user's ID extracted from {@link AbstractUser#uri}.
     *
     * @return returns id extracted from {@link AbstractUser#uri}
     */
    public String getId() {
        return uri != null
               ? uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1)
               : null;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractUser that = (AbstractUser) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public String toString() {
        return "AbstractUser{"
            + "uri=" + uri
            + ", firstName='" + firstName + '\''
            + ", lastName='" + lastName + '\''
            + '}';
    }
}
