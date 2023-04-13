package com.github.checkit.model;

import com.github.checkit.exception.LoadingCustomEntityException;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import java.net.URI;
import java.util.Objects;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@OWLClass(iri = RDFS.RESOURCE)
public class ObjectResource implements HasIdentifier {

    static Logger logger = LoggerFactory.getLogger(ObjectResource.class);

    //TODO: remove annotations and id needed for OWLClass after new JOPA is released (https://github.com/kbss-cvut/jopa/issues/153)
    @Id(generated = false)
    private URI uri;

    public static String TYPE_SEPARATOR = "^^";
    public static String LANG_SEPARATOR = "@";
    public static String BLANK_NODE_SEPARATOR = "_b:";

    @OWLDataProperty(iri = "http://example.com/value")
    private String value;
    @OWLDataProperty(iri = "http://example.com/type")
    private URI type;
    @OWLDataProperty(iri = "http://example.com/lang")
    private String language;
    @OWLDataProperty(iri = "http://example.com/bnode")
    private Boolean blankNode;

    /**
     * Constructor.
     */
    public ObjectResource() {
        this.uri = URI.create("http://random.com");
        this.value = null;
        this.type = null;
        this.language = null;
        this.blankNode = true;
    }

    //TODO: remove

    /**
     * Constructor.
     */
    public ObjectResource(String value, URI type, String language) {
        this.uri = URI.create("http://random.com");
        this.value = value;
        this.type = type;
        this.language = language;
        this.blankNode = false;
    }

    /**
     * Serializes object into axiom string.
     *
     * @return axiom string
     */
    public String toAxiom() {
        StringBuilder sb = new StringBuilder();
        if (Objects.nonNull(value)) {
            sb.append(value);
        }
        sb.append(TYPE_SEPARATOR);
        if (Objects.nonNull(type)) {
            sb.append(type);
        }
        sb.append(LANG_SEPARATOR);
        if (Objects.nonNull(language)) {
            sb.append(language);
        }
        sb.append(BLANK_NODE_SEPARATOR);
        sb.append(blankNode);
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
        boolean blankNode;
        try {
            //find if is blank node
            String temp = value.substring(value.lastIndexOf(BLANK_NODE_SEPARATOR));
            blankNode = Boolean.parseBoolean(temp.substring(BLANK_NODE_SEPARATOR.length()));
            if (blankNode) {
                return new ObjectResource();
            }
            value = value.substring(0, value.length() - temp.length());
            //find language tag
            temp = value.substring(value.lastIndexOf(LANG_SEPARATOR));
            if (temp.length() == 2 + LANG_SEPARATOR.length()) {
                lang = temp.substring(LANG_SEPARATOR.length());
            }
            value = value.substring(0, value.length() - temp.length());
            //find type
            temp = value.substring(value.lastIndexOf(TYPE_SEPARATOR));
            if (!temp.substring(TYPE_SEPARATOR.length()).isEmpty()) {
                type = URI.create(temp.substring(TYPE_SEPARATOR.length()));
            }
            value = value.substring(0, value.length() - temp.length());
        } catch (StringIndexOutOfBoundsException e) {
            throw LoadingCustomEntityException.create(ObjectResource.class);
        }
        return new ObjectResource(value, type, lang);
    }
}
