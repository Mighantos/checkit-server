package com.github.checkit.util;

/**
 * Vocabulary for CheckIt server model.
 */
public final class TermVocabulary {

    /**
     * Namespace (prefix) definition.
     */
    public static final String SLOVNIK_GOV_CZ = "https://slovník.gov.cz";
    public static final String WORKSPACE_NAMESPACE =
        "https://slovník.gov.cz/datový/pracovní-prostor/pojem/";
    public static final String DATA_DESCRIPTION_NAMESPACE =
        "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/";
    public static final String CHANGE_DESCRIPTION_NAMESPACE =
        "https://slovník.gov.cz/datový/popis-zmen/pojem/";

    /**
     * Terms definition.
     */
    public static final String s_p_ma_krestni_jmeno = DATA_DESCRIPTION_NAMESPACE
        + "má-křestní-jméno";
    public static final String s_p_ma_prijmeni = DATA_DESCRIPTION_NAMESPACE
        + "má-příjmení";
    public static final String s_c_uzivatel = DATA_DESCRIPTION_NAMESPACE
        + "uživatel";
    public static final String s_c_slovnikovy_kontext = WORKSPACE_NAMESPACE + "slovníkový-kontext";
    public static final String s_c_prilohovy_kontext = WORKSPACE_NAMESPACE + "přílohový-kontext";
    public static final String s_c_slovnik = DATA_DESCRIPTION_NAMESPACE + "slovník";
    public static final String s_p_vychazi_z_verze = WORKSPACE_NAMESPACE + "vychází-z-verze";
    public static final String s_p_odkazuje_na_prilohovy_kontext = WORKSPACE_NAMESPACE
        + "odkazuje-na-přílohový-kontext";

    public static final String s_p_ma_datum_a_cas_vytvoreni = DATA_DESCRIPTION_NAMESPACE + "má-datum-a-čas-vytvoření";

    public static final String s_p_ma_gestora = CHANGE_DESCRIPTION_NAMESPACE + "má-gestora";
    public static final String s_p_je_gestorem = CHANGE_DESCRIPTION_NAMESPACE + "je-gestorem";
    public static final String s_c_pozadavek_na_gestorovani = CHANGE_DESCRIPTION_NAMESPACE + "požadavek-na-gestorování";
    public static final String s_p_ma_zadatele = CHANGE_DESCRIPTION_NAMESPACE + "má-žadatele";
    public static final String s_p_ma_zada_o_gestorovani = CHANGE_DESCRIPTION_NAMESPACE + "žádá-o-gestorování";
}
