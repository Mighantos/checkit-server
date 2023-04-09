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
    public static final String PRIMARY_VOCABULARY_NAMESPACE = "https://slovník.gov.cz/základní/pojem/";
    public static final String DATA_DESCRIPTION_NAMESPACE =
        "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/";
    public static final String CHANGE_DESCRIPTION_NAMESPACE =
        "https://slovník.gov.cz/datový/popis-zmen/pojem/";
    public static final String SIOC_NAMESPACE = "http://rdfs.org/sioc/ns#";
    public static final String SIOCT_NAMESPACE = "http://rdfs.org/sioc/types#";

    /**
     * Change description terms definition.
     */
    public static final String s_c_pozadavek_na_gestorovani = CHANGE_DESCRIPTION_NAMESPACE + "požadavek-na-gestorování";
    public static final String s_c_publikacni_kontext = CHANGE_DESCRIPTION_NAMESPACE + "publikační-kontext";
    public static final String s_c_zmena = CHANGE_DESCRIPTION_NAMESPACE + "změna";
    public static final String s_c_typ_zmeny = CHANGE_DESCRIPTION_NAMESPACE + "typ";
    public static final String s_c_vytvoreno = CHANGE_DESCRIPTION_NAMESPACE + "vytvořeno";
    public static final String s_c_upraveno = CHANGE_DESCRIPTION_NAMESPACE + "upraveno";
    public static final String s_c_odstraneno = CHANGE_DESCRIPTION_NAMESPACE + "odstraněno";
    public static final String s_c_vraceno_zpet = CHANGE_DESCRIPTION_NAMESPACE + "vráceno-zpět";
    public static final String s_p_ma_gestora = CHANGE_DESCRIPTION_NAMESPACE + "má-gestora";
    public static final String s_p_je_gestorem = CHANGE_DESCRIPTION_NAMESPACE + "je-gestorem";
    public static final String s_p_ma_zadatele = CHANGE_DESCRIPTION_NAMESPACE + "má-žadatele";
    public static final String s_p_zada_o_gestorovani = CHANGE_DESCRIPTION_NAMESPACE + "žádá-o-gestorování";
    public static final String s_p_ma_pozadavek_na_gestorovani = CHANGE_DESCRIPTION_NAMESPACE
        + "má-požadavek-na-gestorování";
    public static final String s_p_z_projektu = CHANGE_DESCRIPTION_NAMESPACE + "z-projektu";
    public static final String s_p_ma_zmenu = CHANGE_DESCRIPTION_NAMESPACE + "má-změnu";
    public static final String s_p_ma_novy_objekt = CHANGE_DESCRIPTION_NAMESPACE + "má-nový-objekt";
    public static final String s_p_v_kontextu = CHANGE_DESCRIPTION_NAMESPACE + "v-kontextu";
    public static final String s_p_schvaleno = CHANGE_DESCRIPTION_NAMESPACE + "schváleno";
    public static final String s_p_zamitnuto = CHANGE_DESCRIPTION_NAMESPACE + "zamítnuto";
    public static final String s_p_je_typu = CHANGE_DESCRIPTION_NAMESPACE + "je-typu";
    public static final String s_p_ma_popis_typu_subjektu = CHANGE_DESCRIPTION_NAMESPACE + "má-popis-typu-subjektu";

    /**
     * Other terms definition.
     */
    public static final String s_c_uzivatel = DATA_DESCRIPTION_NAMESPACE + "uživatel";
    public static final String s_c_kontext = WORKSPACE_NAMESPACE + "kontext";
    public static final String s_c_metadatovy_kontext = WORKSPACE_NAMESPACE + "metadatový-kontext";
    public static final String s_c_slovnikovy_kontext = WORKSPACE_NAMESPACE + "slovníkový-kontext";
    public static final String s_c_prilohovy_kontext = WORKSPACE_NAMESPACE + "přílohový-kontext";
    public static final String s_c_slovnik = DATA_DESCRIPTION_NAMESPACE + "slovník";
    public static final String s_p_ma_krestni_jmeno = DATA_DESCRIPTION_NAMESPACE + "má-křestní-jméno";
    public static final String s_p_ma_prijmeni = DATA_DESCRIPTION_NAMESPACE + "má-příjmení";
    public static final String s_p_vychazi_z_verze = WORKSPACE_NAMESPACE + "vychází-z-verze";
    public static final String s_p_odkazuje_na_kontext = WORKSPACE_NAMESPACE + "odkazuje-na-kontext";
    public static final String s_p_odkazuje_na_prilohovy_kontext = WORKSPACE_NAMESPACE
        + "odkazuje-na-přílohový-kontext";
    public static final String s_p_ma_datum_a_cas_vytvoreni = DATA_DESCRIPTION_NAMESPACE + "má-datum-a-čas-vytvoření";
    public static final String s_p_ma_datum_a_cas_posledni_modifikace = DATA_DESCRIPTION_NAMESPACE
        + "má-datum-a-čas-poslední-modifikace";
    public static final String s_p_ma_vztazeny_prvek_1 = PRIMARY_VOCABULARY_NAMESPACE + "má-vztažený-prvek-1";
    public static final String s_p_has_creator = SIOC_NAMESPACE + "has_creator";
    public static final String s_p_topic = SIOC_NAMESPACE + "topic";
    public static final String s_p_content = SIOC_NAMESPACE + "content";
    public static final String s_c_Comment = SIOCT_NAMESPACE + "Comment";
}
