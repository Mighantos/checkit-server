package com.github.checkit.environment;

import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.io.IOException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

public class Environment {

    public static final String BASE_URI = TermVocabulary.CHANGE_DESCRIPTION_NAMESPACE;

    /**
     * Adds Change description ontology to the database.
     *
     * @param em Entity manager
     */
    public static void addModelStructureForRdfsInference(EntityManager em) {
        final Repository repo = em.unwrap(Repository.class);
        try (final RepositoryConnection conn = repo.getConnection()) {
            conn.begin();
            conn.add(Environment.class.getClassLoader().getResourceAsStream("ontology/d-sgov-popis-zmen-glosář.ttl"),
                BASE_URI,
                RDFFormat.TURTLE);
            conn.add(Environment.class.getClassLoader().getResourceAsStream("ontology/d-sgov-popis-zmen-model.ttl"),
                BASE_URI,
                RDFFormat.TURTLE);
            conn.add(Environment.class.getClassLoader().getResourceAsStream("ontology/d-sgov-popis-zmen-slovník.ttl"),
                BASE_URI,
                RDFFormat.TURTLE);
            conn.commit();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load CheckIt model for import.", e);
        }
    }
}
