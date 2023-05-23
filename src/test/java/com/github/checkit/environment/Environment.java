package com.github.checkit.environment;

import com.github.checkit.model.User;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.io.IOException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class Environment {

    public static final String BASE_URI = TermVocabulary.CHANGE_DESCRIPTION_NAMESPACE;

    private static User currentUser;

    /**
     * Initializes security context with the specified user.
     *
     * @param user User to set as currently authenticated
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
//        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken();
        SecurityContext context = new SecurityContextImpl();
//        context.setAuthentication(jwtAuthenticationToken);
        SecurityContextHolder.setContext(context);
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Resets security context, removing any previously set data.
     */
    public static void resetCurrentUser() {
        currentUser = null;
        SecurityContextHolder.clearContext();
    }

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
