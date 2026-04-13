package cte.testfaelle.consistency;

import cte.testfaelle.ITSQTestFaelleUtilTest;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import de.creditreform.crefoteam.cte.archivbestand30.util.AB30JaxbUtil;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test der Rohdaten mit komplettem JAXB-Parsing des AB30
 */
public class RohdatenConsistencyAB30Test extends RohdatenConsistencyBase {

    protected static final AB30JaxbUtil AB30_JAXB_UTIL = new AB30JaxbUtil();

    @Rule
    public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();

    public RohdatenConsistencyAB30Test() {
    }

    @Test
    public void testRohdatenBeteiligte() {
        TimelineLogger.info(getClass(), "================== testRohdatenBeteiligte ==================");
        RohdatenParserIF parser = new RohdatenParserJaxb(AB30_JAXB_UTIL);
        Map<Long, RohdatenBeschreibung> targetMapP1 = scanParentDir(parser, new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1));
        Map<Long, RohdatenBeschreibung> targetMapP2 = scanParentDir(parser, new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2));

        verifyBtlgP1P2(targetMapP1, targetMapP2);
        logBtlgExtendP2(targetMapP2);
        TimelineLogger.info(getClass(), "\n------------------ testRohdatenBeteiligte ------------------\n");
    }

    /**
     * Wir loggen, wie viele Beteiligte aus Phase 2 per 'extend' entstehen
     */
    private void logBtlgExtendP2(Map<Long, RohdatenBeschreibung> targetMapP2) {
        Set<Long> btlgSet = new HashSet<>();
        int anzBtlgPerExtend = 0;
        for (Map.Entry<Long, RohdatenBeschreibung> e2 : targetMapP2.entrySet()) {
            final List<Long> beteiligte = e2.getValue().getBeteiligte();
            if (beteiligte != null && !beteiligte.isEmpty()) {
                btlgSet.addAll(beteiligte);
            }
        }
        for (Long btlg : btlgSet) {
            if (!targetMapP2.containsKey(btlg)) {
                anzBtlgPerExtend++;
            }
        }
        TimelineLogger.info(getClass(), "Insgesamt {} Crefos in Phase 2, {} Beteiligte enstehen per 'extend'", targetMapP2.size(), anzBtlgPerExtend);
    }

    /**
     * Wir suchen Beteiligte in P1, die nur in P2 existieren...
     */
    private void verifyBtlgP1P2(Map<Long, RohdatenBeschreibung> targetMapP1, Map<Long, RohdatenBeschreibung> targetMapP2) {
        List<String> errorMessages = new ArrayList<>();
        int anzBtlg = 0;
        for (Map.Entry<Long, RohdatenBeschreibung> e1 : targetMapP1.entrySet()) {
            final List<Long> beteiligte = e1.getValue().getBeteiligte();
            if (beteiligte != null && !beteiligte.isEmpty()) {
                for (Long btlg : beteiligte) {
                    anzBtlg++;
                    if (!targetMapP2.containsKey(btlg) && targetMapP1.containsKey(btlg)) {
                        errorMessages.add(e1.getValue().getRohdatenFile().getName() + ": Beteiligte Crefo " + btlg + " existiert nicht in Phase 1, wird aber in Phase 2 benutzt");
                    }
                }
            }
        }
        if (!errorMessages.isEmpty()) {
            Assert.fail(errorMessages.toString());
        }
        Assert.assertTrue("keine Beteiligten gefunden", anzBtlg > 0);
        TimelineLogger.info(getClass(), "{} Crefos aus Phase 1 mit {} Beteiligten gepr\u00fcft", targetMapP1.size(), anzBtlg);
    }

}
