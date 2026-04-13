package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class TrdiRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER = "crefonummer>";
    private static final String TAG_STAMM_DATEN = "trdi_firmendatenexport>";
    private static final String TAG_BTLG_DATEN = "trdi_beteiligtenexport>";

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
        } else if (existsXmlTAG(line, TAG_BTLG_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.BETEILGTEN_DATEN;
        }
        return refExportType;
    }
}
