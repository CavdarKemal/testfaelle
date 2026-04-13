package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class NimRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER = "crefonummer>";
    private static final String TAG_STAMM_DATEN_FIRMA = "nim-firmendaten>";
    private static final String TAG_STAMM_DATEN_BTLG = "nim-personendaten>";

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
        if (existsXmlTAG(line, TAG_STAMM_DATEN_FIRMA)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.CREFO_DATEN_FIRMA;
        } else if (existsXmlTAG(line, TAG_STAMM_DATEN_BTLG)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.CREFO_DATEN_PERSON;
        }
        return refExportType;
    }
}
