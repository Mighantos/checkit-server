package com.github.checkit.model;

import com.github.checkit.model.auxilary.AbstractEntity;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import java.net.URI;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@OWLClass(iri = TermVocabulary.s_c_objekt_zmeny)
public class ChangeObject extends AbstractEntity {

    @OWLDataProperty(iri = TermVocabulary.s_p_ma_hodnotu)
    private MultilingualString valueWithLanguageTag;

    @OWLAnnotationProperty(iri = TermVocabulary.s_p_ma_typ_hodnoty)
    private URI type;

    /**
     * Constructor.
     */
    public ChangeObject(String value, URI type, String language) {
        this.valueWithLanguageTag = new MultilingualString();
        this.valueWithLanguageTag.set(language, value);
        this.type = type;
    }

    public boolean isBlankNode() {
        return Objects.isNull(valueWithLanguageTag);
    }

    public String getValue() {
        return isBlankNode() ? null : valueWithLanguageTag.get();
    }

    public String getLanguage() {
        return isBlankNode() ? null : this.valueWithLanguageTag.getLanguages().iterator().next();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChangeObject that = (ChangeObject) o;
        return Objects.equals(valueWithLanguageTag, that.valueWithLanguageTag) && Objects.equals(type,
            that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueWithLanguageTag, type);
    }
}
