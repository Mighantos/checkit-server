package com.github.checkit.model;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.annotations.Individual;

public enum ChangeType {
    @Individual(iri = TermVocabulary.s_c_vytvoreno)
    CREATED,
    @Individual(iri = TermVocabulary.s_c_upraveno)
    MODIFIED,
    @Individual(iri = TermVocabulary.s_c_odstraneno)
    REMOVED,
    @Individual(iri = TermVocabulary.s_c_vraceno_zpet)
    ROLLBACKED
}
