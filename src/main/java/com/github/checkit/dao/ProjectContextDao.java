package com.github.checkit.dao;

import com.github.checkit.exception.PersistenceException;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.persistence.DescriptorFactory;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectContextDao extends BaseDao<ProjectContext> {

    private final DescriptorFactory descriptorFactory;

    protected ProjectContextDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(ProjectContext.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public Optional<ProjectContext> find(URI id) {
        Objects.requireNonNull(id);
        try {
            Descriptor descriptor = descriptorFactory.projectContextDescriptor(id);
            return Optional.ofNullable(em.find(type, id, descriptor));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds the labels of an IRI entity in a scope of specified project.
     *
     * @param projectContextUri project context where label should be searched
     * @param iri               URI identifier of entity
     * @return multilingual string
     */
    public MultilingualString getPrefLabelOfIRI(URI projectContextUri, URI iri) {
        Objects.requireNonNull(projectContextUri);
        Objects.requireNonNull(iri);
        try {
            MultilingualString multilingualString = new MultilingualString();
            em.createNativeQuery("SELECT DISTINCT ?lang ?label WHERE { "
                    + "?project ?linksToContext ?graph . "
                    + "GRAPH ?graph { "
                    + "     ?iri ?prefLabel|?title ?literal . "
                    + "     BIND(STR(?literal) as ?label) "
                    + "     BIND(LANG(?literal) as ?lang) "
                    + "}}")
                .setParameter("project", projectContextUri)
                .setParameter("linksToContext", URI.create(TermVocabulary.s_p_odkazuje_na_kontext))
                .setParameter("iri", iri)
                .setParameter("title", URI.create(DC.Terms.TITLE))
                .setParameter("prefLabel", URI.create(SKOS.PREF_LABEL))
                .getResultList().forEach(label -> multilingualString.set(((Object[]) label)[0].toString(),
                    ((Object[]) label)[1].toString()));
            return multilingualString;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Finds the labels of an IRI entity.
     *
     * @param iri URI identifier of entity
     * @return multilingual string
     */
    public MultilingualString getPrefLabelOfIRI(URI iri) {
        Objects.requireNonNull(iri);
        try {
            MultilingualString multilingualString = new MultilingualString();
            em.createNativeQuery("SELECT DISTINCT ?lang ?label WHERE { "
                    + "?iri ?prefLabel|?title ?literal . "
                    + "BIND(STR(?literal) as ?label) "
                    + "BIND(LANG(?literal) as ?lang) "
                    + "}")
                .setParameter("iri", iri)
                .setParameter("title", URI.create(DC.Terms.TITLE))
                .setParameter("prefLabel", URI.create(SKOS.PREF_LABEL))
                .getResultList().forEach(label -> multilingualString.set(((Object[]) label)[0].toString(),
                    ((Object[]) label)[1].toString()));
            return multilingualString;
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
