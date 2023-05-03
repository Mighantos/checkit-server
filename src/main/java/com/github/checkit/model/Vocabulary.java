package com.github.checkit.model;

import com.github.checkit.model.auxilary.AbstractEntity;
import com.github.checkit.model.auxilary.HasTypes;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.Inferred;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.vocabulary.DC;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_slovnik)
public class Vocabulary extends AbstractEntity implements HasTypes {

    @OWLDataProperty(iri = DC.Terms.TITLE)
    private String label;

    @OWLObjectProperty(iri = TermVocabulary.s_p_ma_gestora, fetch = FetchType.EAGER)
    private Set<User> gestors = new HashSet<>();

    @Inferred
    @OWLObjectProperty(iri = TermVocabulary.s_p_ma_pozadavek_na_gestorovani)
    private Set<URI> gestoringRequests;

    public void addGestor(User user) {
        getGestors().add(user);
    }

    public void removeGestor(User user) {
        getGestors().remove(user);
    }

    public String toSimpleString() {
        return String.format("{%s,  \"%s\"}", getLabel(), getUri());
    }
}
