package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class IsmRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER = "crefo-number>";
    private static final String TAG_STAMM_DATEN = "update-on-company>";

    @Override
    public String getClzString(String line) {
        return RefExportsBeschreibung.NOT_SUPPORTED;
    }

    public String getCrefoNummerString(String line) {
        String crefoString = getStringForXmlTag(line, TAG_CREFONUMMER, 10);
        if (crefoString == null) {
        }
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
