package com.github.checkit.environment;

import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.model.VocabularyContext;
import java.net.URI;
import java.util.Objects;
import java.util.Random;

public class Generator {

    private static final Random random = new Random();

    /**
     * Generates a (pseudo) random URI with optional class name, usable for test individuals.
     *
     * @return Random URI
     */
    public static URI generateUri(String className) {
        if (Objects.isNull(className) || className.isEmpty()) {
            className = "unknown";
        }
        return URI.create(Environment.BASE_URI + "randomInstance/" + className + "/" + random.nextLong());
    }

    /**
     * Generates a (pseudo) random URI with class name, usable for test individuals.
     *
     * @return Random URI
     */
    public static URI generateUri(Class c) {
        return generateUri(c.getSimpleName());
    }

    /**
     * Generates a (pseudo) random URI, usable for test individuals.
     *
     * @return Random URI
     */
    public static URI generateUri() {
        return generateUri((String) null);
    }

    /**
     * Generates default user.
     *
     * @param usernamePrefix prefix before username
     * @return user
     */
    public static User generateDefaultUser(String usernamePrefix) {
        User user = new User();
        user.setUri(URI.create(usernamePrefix + "user"));
        user.setFirstName("User");
        user.setLastName("Test");
        return user;
    }

    /**
     * Generates a vocabulary with random URI.
     *
     * @return vocabulary
     */
    public static Vocabulary generateVocabularyWithUri() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setUri(generateUri(Vocabulary.class));
        return vocabulary;
    }


    /**
     * Generates a vocabulary context with random URI.
     *
     * @return vocabulary context
     */
    public static VocabularyContext generateVocabularyContextWithUri() {
        VocabularyContext vocabularyContext = new VocabularyContext();
        vocabularyContext.setUri(generateUri(VocabularyContext.class));
        return vocabularyContext;
    }

    /**
     * Generates a project context with random URI.
     *
     * @return project context
     */
    public static ProjectContext generateProjectContextWithUri() {
        ProjectContext projectContext = new ProjectContext();
        projectContext.setUri(generateUri(ProjectContext.class));
        return projectContext;
    }


    /**
     * Generates a publication context with random URI.
     *
     * @return publication context
     */
    public static PublicationContext generatePublicationContextWithUri() {
        PublicationContext publicationContext = new PublicationContext();
        publicationContext.setUri(generateUri(PublicationContext.class));
        return publicationContext;
    }

    /**
     * Generates a change of type create with random URI.
     *
     * @return create change
     */
    public static Change generateCreateChangeWitUri(VocabularyContext vocabularyContext) {
        Change change = new Change(vocabularyContext);
        URI uri = generateUri(Change.class);
        change.setUri(uri);
        change.setChangeType(ChangeType.CREATED);
        change.setLabel(uri.toString().substring(uri.toString().lastIndexOf("/") + 1));
        change.setSubject(Generator.generateUri());
        change.setPredicate(Generator.generateUri());
        change.setObject(Generator.generateUri().toString());
        return change;
    }
}
