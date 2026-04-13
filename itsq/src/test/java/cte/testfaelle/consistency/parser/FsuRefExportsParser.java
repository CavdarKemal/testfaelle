package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class FsuRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER1 = "crefonummer>";
    private static final String TAG_CREFONUMMER2 = "fsu-crefonummer>";
    private static final String TAG_STAMM_DATEN_FIRMA = "fsu-firmendaten>";
    private static final String TAG_LOESCH_SATZ = "fsu-loeschsatzexport";

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
        if (existsXmlTAG(line, TAG_STAMM_DATEN_FIRMA)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.CREFO_DATEN_FIRMA;
        }
        if (existsXmlTAG(line, TAG_LOESCH_SATZ)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.LOESCH_SATZ;
        }

        return refExportType;
    }
}
