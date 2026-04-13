package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class CtcRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER = "crefonummer>";
    private static final String TAG_STAMM_DATEN = "vvc-datensatzliste>";
    private static final String TAG_DATEN_VC = "datenVC>";

    @Override
    public String getClzString(String line) {
        return getStringForXmlTag(line, TAG_DATEN_VC, 3);
    }

    @Override
    public String getCrefoNummerString(String line) {
        String crefoString = getStringForXmlTag(line, TAG_CREFONUMMER, 10);
        return crefoString;
    }

    @Override
    public RefExportsBeschreibung.REF_EXPORT_TYPE getRefExportType(String line) {
        RefExportsBeschreibung.REF_EXPORT_TYPE refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.UNBEKANNT;
        if (existsXmlTAG(line, TAG_STAMM_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.CREFO_DATEN_FIRMA;
        }
        return refExportType;
    }
}
