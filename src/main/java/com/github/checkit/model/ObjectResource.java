package com.github.checkit.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import java.net.URI;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@NoArgsConstructor
@AllArgsConstructor
@OWLClass(iri = RDFS.RESOURCE)
public class ObjectResource implements HasIdentifier {

    static Logger logger = LoggerFactory.getLogger(ObjectResource.class);

    //TODO: remove OWLClass annotation after new JOPA is released
    @Id(generated = false)
    private URI uri;

    public static String TYPE_SEPARATOR = "^^";
    public static String LANG_SEPARATOR = "@";

    @OWLDataProperty(iri = "http://example.com/value")
    private String value;
    @OWLDataProperty(iri = "http://example.com/type")
    private URI type;
    @OWLDataProperty(iri = "http://example.com/lang")
    private String language;

    //TODO: remove

    /**
     * Constructor.
     */
    public ObjectResource(String value, URI type, String language) {
        this.uri = URI.create("http://random.com");
        this.value = value;
        this.type = type;
        this.language = language;
    }

    /**
     * Serializes object into axiom string.
     *
     * @return axiom string
     */
    public String toAxiom() {
        StringBuilder sb = new StringBuilder(value);
        if (Objects.nonNull(type)) {
            sb.append(TYPE_SEPARATOR);
            sb.append(type);
        }
        if (Objects.nonNull(language) && !language.isEmpty()) {
            sb.append(LANG_SEPARATOR);
            sb.append(language);
        }
        return sb.toString();
    }

    /**
     * Resolves string to ObjectResource.
     *
     * @param serialized serialized ObjectResource
     * @return ObjectResource
     */
    public static ObjectResource of(String serialized) {
        String value = serialized;
        URI type = null;
        String lang = null;
        if (serialized.contains(LANG_SEPARATOR)) {
            String temp = serialized.substring(serialized.lastIndexOf(LANG_SEPARATOR));
            if (temp.length() == 2 + LANG_SEPARATOR.length()) {
                lang = temp.substring(LANG_SEPARATOR.length());
                value = value.substring(0, value.length() - temp.length());
            }
        }
        if (serialized.contains(TYPE_SEPARATOR)) {
            String temp = value.substring(serialized.lastIndexOf(TYPE_SEPARATOR));
            try {
                type = URI.create(temp.substring(TYPE_SEPARATOR.length()));
                value = value.substring(0, value.length() - temp.length());
            } catch (Exception e) {
                //can happen that text contains TYPE_SEPARATOR
                logger.trace("Type separator {} was not followed by URI.", TYPE_SEPARATOR);
            }
        }
        return new ObjectResource(value, type, lang);
    }
}
