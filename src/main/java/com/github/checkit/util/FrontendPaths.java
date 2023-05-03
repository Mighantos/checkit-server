package com.github.checkit.util;

/**
 * This class contains some URL paths of Frontend CheckIt used in notifications.
 */
public final class FrontendPaths {

    private static final String PUBLICATION_CONTEXT_PATH_NAME = "publications";
    private static final String VOCABULARY_IN_PUBLICATION_CONTEXT_PATH_NAME = "vocabulary";
    private static final String ADMIN_PANEL_PATH_NAME = "administration";

    private static final String VOCABULARY_URI_PARAMETER_NAME = "vocabularyUri";
    private static final String CHANGE_ID_PARAMETER_NAME = "changeId";

    public static String getPublicationDetailPath(String publicationId) {
        return String.format("/%s/%s", PUBLICATION_CONTEXT_PATH_NAME, publicationId);
    }

    public static String getVocabularyInPublicationContextPath(String publicationId, String vocabularyUri) {
        return String.format("%s/%s?%s=%s", getPublicationDetailPath(publicationId),
            VOCABULARY_IN_PUBLICATION_CONTEXT_PATH_NAME, VOCABULARY_URI_PARAMETER_NAME, vocabularyUri);
    }

    public static String getChangeInVocabularyInPublicationContextPath(String publicationId, String vocabularyUri,
                                                                       String changeId) {
        return String.format("%s&%s=%s", getVocabularyInPublicationContextPath(publicationId, vocabularyUri),
            CHANGE_ID_PARAMETER_NAME, changeId);
    }

    public static String getGestoringRequests() {
        return String.format("/%s", ADMIN_PANEL_PATH_NAME);
    }
}
