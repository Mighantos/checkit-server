package com.github.checkit.environment;

import com.github.checkit.model.Change;
import com.github.checkit.model.ChangeObject;
import com.github.checkit.model.ChangeType;
import com.github.checkit.model.ProjectContext;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import com.github.checkit.model.VocabularyContext;
import com.github.checkit.model.auxilary.ChangeSubjectType;
import com.github.checkit.util.TermVocabulary;
import cz.cvut.kbss.jopa.model.MultilingualString;
import java.net.URI;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class Generator {

    private static final Random random = new Random();

    private static int generatePositiveInt() {
        return random.nextInt(200000000);
    }

    /**
     * Generates a (pseudo) random URI with optional class name, usable for test individuals.
     *
     * @return Random URI
     */
    public static URI generateUri(String className) {
        if (Objects.isNull(className) || className.isEmpty()) {
            className = "unknown";
        }
        return URI.create(Environment.BASE_URI + "randomInstance/" + className + "-" + generatePositiveInt());
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
     * Generates user with specified name.
     *
     * @param name           name of the user
     * @param usernamePrefix prefix before username
     * @return user
     */
    public static User generateUser(String name, String usernamePrefix) {
        User user = new User();
        user.setUri(URI.create(usernamePrefix + name));
        user.setFirstName(name);
        user.setLastName("Test");
        return user;
    }

    /**
     * Generates a vocabulary with random URI.
     *
     * @return vocabulary
     */
    public static Vocabulary generateVocabulary() {
        Vocabulary vocabulary = new Vocabulary();
        URI uri = generateUri(Vocabulary.class);
        vocabulary.setUri(uri);
        vocabulary.setLabel("Vocabulary" + uri.toString().substring(uri.toString().lastIndexOf("/") + 1));
        return vocabulary;
    }

    /**
     * Generates a vocabulary with random URI.
     *
     * @param gestors set of users who gestor this vocabulary
     * @return vocabulary
     */
    public static Vocabulary generateVocabulary(Set<User> gestors) {
        Vocabulary vocabulary = generateVocabulary();
        vocabulary.setGestors(gestors);
        return vocabulary;
    }


    /**
     * Generates a vocabulary context with random URI.
     *
     * @param vocabulary vocabulary the context is based on
     * @return vocabulary context
     */
    public static VocabularyContext generateVocabularyContext(Vocabulary vocabulary) {
        VocabularyContext vocabularyContext = new VocabularyContext();
        vocabularyContext.setUri(generateUri(VocabularyContext.class));
        vocabularyContext.setBasedOnVersion(vocabulary.getUri());
        return vocabularyContext;
    }

    /**
     * Generates a project context with random URI.
     *
     * @param author             author of the project
     * @param vocabularyContexts set of vocabulary contexts in project
     * @return project context
     */
    public static ProjectContext generateProjectContext(User author, Set<VocabularyContext> vocabularyContexts) {
        ProjectContext projectContext = new ProjectContext();
        projectContext.setUri(generateUri(ProjectContext.class));
        projectContext.setLabel("Project" + generatePositiveInt());
        projectContext.setAuthor(author);
        projectContext.setVocabularyContexts(vocabularyContexts);
        return projectContext;
    }


    /**
     * Generates a publication context with random URI.
     *
     * @return publication context
     */
    public static PublicationContext generatePublicationContext(ProjectContext projectContext, Set<Change> changes) {
        PublicationContext publicationContext = new PublicationContext();
        publicationContext.setUri(
            URI.create(TermVocabulary.s_c_publikacni_kontext + "/PublicationContext-" + generatePositiveInt()));
        publicationContext.setCorrespondingPullRequest("NotPublished");
        publicationContext.setFromProject(projectContext);
        publicationContext.setChanges(changes);
        return publicationContext;
    }

    /**
     * Generates a change of type Created with random URI.
     *
     * @return create change
     */
    public static Change generateCreateChange(VocabularyContext vocabularyContext) {
        Change change = new Change(vocabularyContext);
        URI uri = generateUri(Change.class);
        change.setUri(uri);
        change.setChangeType(ChangeType.CREATED);
        change.setSubjectType(ChangeSubjectType.UNKNOWN);
        change.setLabel(new MultilingualString().set(uri.toString().substring(uri.toString().lastIndexOf("/") + 1)));
        change.setSubject(Generator.generateUri());
        change.setPredicate(Generator.generateUri());
        change.setObject(Generator.generateObjectOfChangeWithURI());
        change.setCountable(true);
        return change;
    }

    /**
     * Generates a change of type Rollbacked with random URI.
     *
     * @return create change
     */
    public static Change generateRollbackedChange(VocabularyContext vocabularyContext) {
        Change change = new Change(vocabularyContext);
        URI uri = generateUri(Change.class);
        change.setUri(uri);
        change.setChangeType(ChangeType.ROLLBACKED);
        change.setSubjectType(ChangeSubjectType.UNKNOWN);
        change.setLabel(new MultilingualString().set(uri.toString().substring(uri.toString().lastIndexOf("/") + 1)));
        change.setSubject(Generator.generateUri());
        change.setPredicate(Generator.generateUri());
        change.setObject(Generator.generateObjectOfChangeWithURI());
        change.setCountable(false);
        return change;
    }

    /**
     * Generates an ChangeObject for a change with random URI as the content.
     *
     * @return publication context
     */
    public static ChangeObject generateObjectOfChangeWithURI() {
        ChangeObject changeObject = new ChangeObject();
        changeObject.setValueWithLanguageTag(new MultilingualString().set(Generator.generateUri().toString()));
        return changeObject;
    }
}
