package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class FwRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER1 = "crefonummer>";
    private static final String TAG_CREFONUMMER2 = "fw-crefonummer>";
    private static final String TAG_STAMM_DATEN_FIRMA = "fw-firmendaten>";
    private static final String TAG_BTLG_DATEN = "fw-beteiligte-person>";
    private static final String TAG_LOESCHSATZ_DATEN = "<fw-loeschsatzexport";

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
        } else if (existsXmlTAG(line, TAG_BTLG_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.BETEILGTEN_DATEN;
        } else if (existsXmlTAG(line, TAG_LOESCHSATZ_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.LOESCH_SATZ;
        }
        return refExportType;
    }
}
