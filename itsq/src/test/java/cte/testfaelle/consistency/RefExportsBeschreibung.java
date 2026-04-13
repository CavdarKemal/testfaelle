package cte.testfaelle.consistency;

import cte.testfaelle.domain.TestCrefo;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Container für die Beschreibung einer Datei mit REF-Exports für den Test
 */
public class RefExportsBeschreibung {

    public enum REF_EXPORT_TYPE {UNBEKANNT, CREFO_DATEN_FIRMA, CREFO_DATEN_PERSON, BETEILGTEN_DATEN, LOESCH_SATZ, NOTIFICATION_OF_DELETION}

    public static final String NOT_SUPPORTED = "NOT_SUPPORTED";
    private static final List<String> CUSTOMERS_NOT_HAVING_CLZ = Arrays.asList("atf", "bdr", "bvd", "cef", "eh", "gdl", "inso_kundenplz", "inso_test-tool", "ism", "len", "mic", "mip", "nim");

    private final String customerKey;
    private final TestCrefo testCrefo;
    private final REF_EXPORT_TYPE refExportType;
    private final String clzString;

    public RefExportsBeschreibung(String customerKey, TestCrefo testCrefo, REF_EXPORT_TYPE refExportType, String clzString) {
        this.testCrefo = testCrefo;
        this.customerKey = customerKey;
        this.refExportType = refExportType;
        this.clzString = setClzString(clzString);
    }

    private String setClzString(String clzString) {
        if (clzString == null || clzString.isEmpty() || clzString.startsWith("nu")) {
            // TestUtil.TimelineLogger.info(getClass(), "\t\t\t\t!!!!! " + TestUtil.getShortPath(getRefExportsFile(), TestSupportClientKonstanten.SRC_REF_EXPORTS_PHASE_1) + " als " + refExportType + " hat kiein CLZ-Info!");
            return NOT_SUPPORTED;
        }
        if (NOT_SUPPORTED.equals(clzString)) {
            boolean inCustomersNotHavingClz = CUSTOMERS_NOT_HAVING_CLZ.contains(customerKey.toLowerCase(Locale.ROOT));
            boolean isLoeschSatz = REF_EXPORT_TYPE.LOESCH_SATZ.equals(refExportType);
            boolean isBtlg = REF_EXPORT_TYPE.BETEILGTEN_DATEN.equals(refExportType);
            if (!inCustomersNotHavingClz && !isBtlg && !isLoeschSatz) {
                String strErr = ITSQTestFaelleUtil.getShortPath(getRefExportsFile(), TestSupportClientKonstanten.REF_EXPORTS_ROOT) + " als " + refExportType + " hat kiein CLZ-Info!";
                TimelineLogger.error(getClass(), "\t\t\t\t!!!!! " + strErr);
                throw new IllegalStateException(strErr);
            }
        }
        return clzString;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public TestCrefo getTestCrefo() {
        return testCrefo;
    }

    public File getRefExportsFile() {
        return testCrefo.getItsqRexExportXmlFile();
    }

    public long getCrefoNummer() {
        return testCrefo.getItsqTestCrefoNr();
    }

    public REF_EXPORT_TYPE getRefExportType() {
        return refExportType;
    }

    public String getClzString() {
        return clzString;
    }

    @Override
    public String toString() {
        return "Crefonummer: " + getCrefoNummer() + "; Typ: " + getRefExportType().name() + "; Clz: " + getClzString() + "; Datei: " + ITSQTestFaelleUtil.getShortPath(getRefExportsFile(), TestSupportClientKonstanten.REF_EXPORTS_ROOT);
    }
}
