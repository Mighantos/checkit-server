package com.github.checkit.persistence;

import com.github.checkit.model.GestoringRequest;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.FieldSpecification;
import java.net.URI;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class DescriptorFactory {

    private final EntityManagerFactory emf;

    public DescriptorFactory(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private static EntityDescriptor entityDescriptor(URI uri) {
        if (Objects.isNull(uri)) {
            return new EntityDescriptor();
        }
        return new EntityDescriptor(uri);
    }

    /**
     * Gets field specification for the specified attribute from persistence unit metamodel.
     *
     * @param entityClass   Entity class
     * @param attributeName Name of attribute in the entity class
     * @return Metamodel field specification
     */
    public <T> FieldSpecification<? super T, ?> fieldSpec(Class<T> entityClass, String attributeName) {
        return emf.getMetamodel().entity(entityClass).getFieldSpecification(attributeName);
    }


    /**
     * Creates a JOPA descriptor for a specific vocabulary.
     *
     * <p>The descriptor specifies that the instance will correspond to the given model.
     * It also initializes other required attribute descriptors.
     *
     * @param vocabulary Vocabulary for which the descriptor should be created
     * @return Vocabulary descriptor
     */
    public Descriptor vocabularyDescriptor(Vocabulary vocabulary) {
        Objects.requireNonNull(vocabulary);
        return vocabularyDescriptor(vocabulary.getUri());
    }

    /**
     * Creates a JOPA descriptor for a vocabulary with the specified identifier.
     *
     * <p>The descriptor specifies that the instance will correspond to the given IRI.
     * It also initializes other required attribute descriptors.
     *
     * @param vocabularyUri Vocabulary identifier for which the descriptor should be created
     * @return Vocabulary descriptor
     */
    public Descriptor vocabularyDescriptor(URI vocabularyUri) {
        Objects.requireNonNull(vocabularyUri);
        EntityDescriptor descriptor = entityDescriptor(vocabularyUri);
        descriptor.addAttributeDescriptor(fieldSpec(Vocabulary.class, "gestors"),
            new EntityDescriptor((URI) null));
        return descriptor;
    }

    /**
     * Creates a JOPA descriptor for a vocabulary with the specified identifier.
     *
     * <p>The descriptor specifies that the instance will correspond to the given IRI.
     * It also initializes other required attribute descriptors.
     *
     * @return Vocabulary descriptor
     */
    public Descriptor gestoringRequestDescriptor() {
        URI contextUri = URI.create(TermVocabulary.s_c_pozadavek_na_gestorovani);
        EntityDescriptor descriptor = entityDescriptor(contextUri);
        descriptor.addAttributeDescriptor(fieldSpec(GestoringRequest.class, "applicant"),
            new EntityDescriptor((URI) null));
        descriptor.addAttributeDescriptor(fieldSpec(GestoringRequest.class, "vocabulary"),
            new EntityDescriptor((URI) null));
        return descriptor;
    }
}
