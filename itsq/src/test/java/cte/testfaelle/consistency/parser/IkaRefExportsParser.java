package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class IkaRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER1 = "Crefonummer>";
    private static final String TAG_CREFONUMMER2 = "delete>";
    private static final String TAG_STAMM_DATEN = "createUpdate>";
    private static final String TAG_DATEN_VC = "Daten-VC>";

    @Override
    public String getClzString(String line) {
        return getStringForXmlTag(line, TAG_DATEN_VC, 3);
    }

    @Override
    public String getCrefoNummerString(String line) {
        String crefoString = getStringForXmlTag(line, TAG_CREFONUMMER1, 10);
        if (crefoString == null) {
            crefoString = getStringForXmlTag(line, TAG_CREFONUMMER2, 10);
        }
        return crefoString;
    }

    @Override
    public RefExportsBeschreibung.REF_EXPORT_TYPE getRefExportType(String line) {
        RefExportsBeschreibung.REF_EXPORT_TYPE refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.UNBEKANNT;
        if (existsXmlTAG(line, TAG_STAMM_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.CREFO_DATEN_FIRMA;
        } else if (existsXmlTAG(line, TAG_CREFONUMMER2)) { // GRRRRRRR!!!!!!!!!!!!!!!!!!!!!!!!!!! Löschsatz kann man sonst nicht erkennen!!!
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.LOESCH_SATZ;
        }
        return refExportType;
    }
}
