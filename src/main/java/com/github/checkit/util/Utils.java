package com.github.checkit.util;

import cz.cvut.kbss.jopa.model.MultilingualString;

public final class Utils {

    /**
     * Gets string of specified language from multilingual string. If it does not exist returns any available string.
     *
     * @param multilingualString multilingual string
     * @param languageTag        preferred language tag
     * @return string
     */
    public static String resolveMultilingual(MultilingualString multilingualString, String languageTag) {
        String res = multilingualString.get(languageTag);
        return res != null ? res : multilingualString.get();
    }

    /**
     * Gets string of specified language from multilingual string. If it does not exist returns string with default
     * language tag or any available string.
     *
     * @param multilingualString multilingual string
     * @param languageTag        preferred language tag
     * @param defaultLanguageTag default language tag
     * @return string
     */
    public static String resolveMultilingual(MultilingualString multilingualString, String languageTag,
                                             String defaultLanguageTag) {
        if (multilingualString.contains(languageTag)) {
            return multilingualString.get(languageTag);
        }
        if (multilingualString.contains(defaultLanguageTag)) {
            return multilingualString.get(defaultLanguageTag);
        }
        return multilingualString.get();
    }
}
