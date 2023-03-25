package com.github.checkit.persistence;

import com.github.checkit.model.Change;
import com.github.checkit.model.GestoringRequest;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
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

    /**
     * Creates a JOPA descriptor for a specific publication context.
     *
     * <p>The descriptor specifies that the instance will correspond to the given model.
     * It also initializes other required attribute descriptors.
     *
     * @param publicationContext Publication context for which the descriptor should be created
     * @return Publication context descriptor
     */
    public Descriptor publicationContextDescriptor(PublicationContext publicationContext) {
        Objects.requireNonNull(publicationContext);
        return publicationContextDescriptor(publicationContext.getUri());
    }

    /**
     * Creates a JOPA descriptor for a publication context with the specified identifier.
     *
     * <p>The descriptor specifies that the instance will correspond to the given IRI.
     * It also initializes other required attribute descriptors.
     *
     * @param publicationContextUri Publication context identifier for which the descriptor should be created
     * @return Publication context descriptor
     */
    public Descriptor publicationContextDescriptor(URI publicationContextUri) {
        Objects.requireNonNull(publicationContextUri);
        EntityDescriptor descriptor = entityDescriptor(publicationContextUri);
        descriptor.addAttributeDescriptor(fieldSpec(PublicationContext.class, "fromProject"),
            new EntityDescriptor((URI) null));
        descriptor.addAttributeDescriptor(fieldSpec(PublicationContext.class, "changes"),
            changeDescriptor(publicationContextUri));
        return descriptor;
    }

    /**
     * Creates a JOPA descriptor for a change located in the specified publication context.
     *
     * <p>The descriptor specifies that the instance will correspond to the given IRI.
     * It also initializes other required attribute descriptors.
     *
     * @param publicationContextUri Publication context identifier in which the change is located
     * @return Change descriptor
     */
    public Descriptor changeDescriptor(URI publicationContextUri) {
        Objects.requireNonNull(publicationContextUri);
        EntityDescriptor descriptor = entityDescriptor(publicationContextUri);
        descriptor.addAttributeDescriptor(fieldSpec(Change.class, "context"),
            new EntityDescriptor((URI) null));
        descriptor.addAttributeDescriptor(fieldSpec(Change.class, "approvedBy"),
            new EntityDescriptor((URI) null));
        descriptor.addAttributeDescriptor(fieldSpec(Change.class, "rejectedBy"),
            new EntityDescriptor((URI) null));
        return descriptor;
    }

    /**
     * Creates a JOPA descriptor for a specific project context.
     *
     * <p>The descriptor specifies that the instance will correspond to the given model.
     * It also initializes other required attribute descriptors.
     *
     * @param projectContext Project context for which the descriptor should be created
     * @return Project context descriptor
     */
    public Descriptor projectContextDescriptor(ProjectContext projectContext) {
        Objects.requireNonNull(projectContext);
        return vocabularyDescriptor(projectContext.getUri());
    }

    /**
     * Creates a JOPA descriptor for a project context with the specified identifier.
     *
     * <p>The descriptor specifies that the instance will correspond to the given IRI.
     * It also initializes other required attribute descriptors.
     *
     * @param projectContextUri Project context identifier for which the descriptor should be created
     * @return Project context descriptor
     */
    public Descriptor projectContextDescriptor(URI projectContextUri) {
        Objects.requireNonNull(projectContextUri);
        EntityDescriptor descriptor = entityDescriptor(projectContextUri);
        descriptor.addAttributeDescriptor(fieldSpec(ProjectContext.class, "vocabularyContexts"),
            new EntityDescriptor((URI) null));
        return descriptor;
    }
}
