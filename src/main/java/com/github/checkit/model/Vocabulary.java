package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.vocabulary.DC;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;


@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = TermVocabulary.s_c_slovnik)
public class Vocabulary extends AbstractEntity implements HasTypes {

    @OWLDataProperty(iri = DC.Terms.TITLE)
    private String label;

    @OWLObjectProperty(iri = TermVocabulary.s_p_ma_gestora, fetch = FetchType.EAGER)
    private Set<User> gestors = new HashSet<>();

    public void addGestor(User user) {
        gestors.add(user);
    }

    public void removeGestor(User user) {
        gestors.remove(user);
    }
}
