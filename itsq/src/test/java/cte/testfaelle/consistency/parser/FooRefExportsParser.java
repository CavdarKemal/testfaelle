package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class FooRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER = "crefonummer>";
    private static final String TAG_FIRMEN_DATEN = "foo-firmendaten>";
    private static final String TAG_BTLG_DATEN = "foo-beteiligtendaten>";

    @Override
    public String getCrefoNummerString(String line) {
        return getStringForXmlTag(line, TAG_CREFONUMMER, 10);
    }

    @Override
    public RefExportsBeschreibung.REF_EXPORT_TYPE getRefExportType(String line) {
        RefExportsBeschreibung.REF_EXPORT_TYPE refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.UNBEKANNT;
        if (existsXmlTAG(line, TAG_FIRMEN_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.CREFO_DATEN_FIRMA;
        } else if (existsXmlTAG(line, TAG_BTLG_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.BETEILGTEN_DATEN;
        }
        return refExportType;
    }
}