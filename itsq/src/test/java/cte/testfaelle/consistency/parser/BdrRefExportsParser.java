package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;

public class BdrRefExportsParser extends DefaultRefExportsParser {
    private static final String TAG_CREFONUMMER0 = "crefonummer>";
    private static final String TAG_CREFONUMMER1 = "crefo-zur-loeschung>";
    private static final String TAG_STAMM_DATEN = "bip_stammdaten>";
    private static final String TAG_BTLG_DATEN = "bip_beteiligtendaten>";
    private static final String TAG_LOESCHSATZ_DATEN = "bip_loeschsatz>";

    @Override
    public String getClzString(String line) {
        return RefExportsBeschreibung.NOT_SUPPORTED;
    }

    @Override
    public String getCrefoNummerString(String line) {
        String crefoString = getStringForXmlTag(line, TAG_CREFONUMMER0, 10);
        if (crefoString == null) {
            crefoString = getStringForXmlTag(line, TAG_CREFONUMMER1, 10);
        }
        return crefoString;
    }

    @Override
    public RefExportsBeschreibung.REF_EXPORT_TYPE getRefExportType(String line) {
        RefExportsBeschreibung.REF_EXPORT_TYPE refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.UNBEKANNT;
        if (existsXmlTAG(line, TAG_STAMM_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.CREFO_DATEN_FIRMA;
        } else if (existsXmlTAG(line, TAG_BTLG_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.BETEILGTEN_DATEN;
        } else if (existsXmlTAG(line, TAG_LOESCHSATZ_DATEN)) {
            refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.LOESCH_SATZ;
        }
        return refExportType;
    }


}
