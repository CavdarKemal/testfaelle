package cte.testfaelle.consistency;

import cte.testfaelle.ITSQTestFaelleUtilTest;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import java.io.File;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test-Klasse zur Überprüfung der Konsistenz innerhalb der PH1-AB30- und PH2-AB30-Dateien
 */
public class RohdatenConsistencyTest extends RohdatenConsistencyBase {

   /*
         - Prüfe, ob die Crefonummer aus dem XML-Dateinamen auch dem Inhalt passt (PH1-AB30- und PH2-AB30-Dateien)
         - Prüfe, ob ein Wechsel Firma <-> Privatperson in den PH1-AB30- und PH2-AB30-Dateien existiert
         - Prüfe, ob der XML-Tag "eigner-vc>" in den PH1-AB30- und PH2-AB30-Dateien identiswh ist
    */

    @Test
    public void testRohdatenConsistency() {
        TimelineLogger.info(getClass(), "\n================== testRohdatenConsistency ==================\n");
        RohdatenParser parser = new RohdatenParser();
        Map<Long, RohdatenBeschreibung> targetMap1 = scanParentDir(parser, new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1));
        Map<Long, RohdatenBeschreibung> targetMap2 = scanParentDir(parser, new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2));
        checkConsistency(targetMap1, targetMap2);
        TimelineLogger.info(getClass(), "\n------------------ testRohdatenConsistency ------------------\n");
    }

    private void checkConsistency(Map<Long, RohdatenBeschreibung> targetMap1, Map<Long, RohdatenBeschreibung> targetMap2) {
        for (Map.Entry<Long, RohdatenBeschreibung> entry : targetMap1.entrySet()) {
            RohdatenBeschreibung beschreibung1 = entry.getValue();
            RohdatenBeschreibung beschreibung2 = targetMap2.get(beschreibung1.getCrefonummer());
            if (beschreibung2 != null) {
                if (beschreibung2.isFirma() != beschreibung1.isFirma()) {
                    Assert.fail("Wechsel Firma <-> Person bei Crefo " + beschreibung1.getCrefonummer() + "\nMap1: " + beschreibung1 + "\nMap2: " + beschreibung2);
                }
                if (beschreibung1.getClzEignerVC() != beschreibung2.getClzEignerVC()) {
                    Assert.fail("Clz-Eigner-VC unterschiedlich bei Crefo " + beschreibung1.getCrefonummer() + "\nMap1: " + beschreibung1 + "\nMap2: " + beschreibung2);
                }
            }
        }
    }
}
