package com.github.checkit.util;

/**
 * Vocabualry for CheckIt server model.
 */
public final class TermVocabulary {

    /**
     * Namespace (prefix) definition
     */
    public static final String SLOVNIK_GOV_CZ = "https://slovník.gov.cz";
    public static final String WORKSPACE_NAMESPACE =
            "https://slovník.gov.cz/datový/pracovní-prostor/pojem/";
    public static final String DATA_DESCRIPTION_NAMESPACE =
            "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/";
    public static final String CHANGE_DESCRIPTION_NAMESPACE =
            "https://slovník.gov.cz/datový/popis-zmen/pojem/";

    public static final String UZIVATEL_ID_PREFIX = SLOVNIK_GOV_CZ +
            "/uživatel/";

    /**
     * Terms definition
     */
    public static final String s_p_ma_krestni_jmeno = DATA_DESCRIPTION_NAMESPACE
            + "má-křestní-jméno";
    public static final String s_p_ma_prijmeni = DATA_DESCRIPTION_NAMESPACE
            + "má-příjmení";
    public static final String s_c_uzivatel = DATA_DESCRIPTION_NAMESPACE
            + "uživatel";
}
