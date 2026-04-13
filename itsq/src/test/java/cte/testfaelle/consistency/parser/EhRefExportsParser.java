package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class EhRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER = "crefonummer>";
    private static final String TAG_STAMM_DATEN_FIRMA = "eh-firmen-daten>";
    private static final String TAG_STAMM_DATEN_PERSON = "eh-personen-daten>";
    private static final String TAG_BTLG_DATEN = "eh-beteiligten-daten>";
    private static final String TAG_LOESCHSATZ_DATEN = "eh-loeschsatz-export>";

    @Override
    public String getClzString(String line) {
        return RefExportsBeschreibung.NOT_SUPPORTED;
    }

    @Override
    public String getCrefoNummerString(String line) {
        String crefoString = getStringForXmlTag(line, TAG_CREFONUMMER, 10);
        return crefoString;
    }

    @Override
    public RefExportsBeschreibung.REF_EXPORT_TYPE getRefExportType(String line) {
        RefExportsBeschreibung.REF_EXPORT_TYPE refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.UNBEKANNT;
        if (existsXmlTAG(line, TAG_STAMM_DATEN_FIRMA)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.CREFO_DATEN_FIRMA;
        } else if (existsXmlTAG(line, TAG_STAMM_DATEN_PERSON)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.CREFO_DATEN_PERSON;
        } else if (existsXmlTAG(line, TAG_BTLG_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.BETEILGTEN_DATEN;
        } else if (existsXmlTAG(line, TAG_LOESCHSATZ_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.LOESCH_SATZ;
        }
        return refExportType;
    }
}
