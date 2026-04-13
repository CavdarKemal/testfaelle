package cte.testfaelle.locateprocessinginstructions;

import cte.testfaelle.ITSQTestFaelleUtilTest;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import de.creditreform.crefoteam.cte.archivbestand30.util.AB30JaxbUtil;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Archivbestand;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Steuerungsdaten;
import de.creditreform.crefoteam.cte.jaxbbasics.jaxbbasicscommon.JaxbBasicsMarshallingException;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.time.FastDateFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ComparePhase1Phase2Job {
    protected static final AB30JaxbUtil jaxbUtil = new AB30JaxbUtil();
    protected static final FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    @Rule
    public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();

    @Before
    public void setUp() {
        Assert.assertTrue(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1).isDirectory());
        Assert.assertTrue(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2).isDirectory());
    }

    @Test
    public void testComparePhase1Phase2() throws IOException {
        Map<Long, Path> mapP1 = ITSQTestFaelleUtil.scanSourceDirectory(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1));
        TimelineLogger.info(getClass(), "Anzahl der Testfälle in Phase 1: {}", mapP1.size());

        Map<Long, Path> mapP2 = ITSQTestFaelleUtil.scanSourceDirectory(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2));
        TimelineLogger.info(getClass(), "Anzahl der Testfälle in Phase 2: {}", mapP2.size());

        int anzahlBeidePhasen = 0;
        Map<String, List<String>> mapCandidates = new HashMap<>();
        for (Map.Entry<Long, Path> entry : mapP2.entrySet()) {
            Path pathP1 = mapP1.get(entry.getKey());
            if (pathP1 != null) {
                anzahlBeidePhasen++;
                compareEntries(mapCandidates, entry.getKey(), pathP1, entry.getValue());
            }
        }
        TimelineLogger.info(getClass(), "Anzahl der Testfälle in beiden Phasen: {}", anzahlBeidePhasen);
        for (Map.Entry<String, List<String>> entry : mapCandidates.entrySet()) {
            String prefix = "Crefo ist ein Kandidat für'" + entry.getKey() + "': {}";
            for (String candidate : entry.getValue()) {
                TimelineLogger.info(getClass(), prefix, candidate);
            }
        }
    }

    private void compareEntries(Map<String, List<String>> mapCandidates, Number crefonummer, Path pathP1, Path pathP2) {
        Archivbestand ab30P1 = parseAB30(pathP1);
        Steuerungsdaten stP1 = ab30P1.getSteuerungsdaten();
        Assert.assertEquals("Crefonummer in den Steuerungsdaten weicht in Phase 1 ab von dem Dateinamen", crefonummer.longValue(), stP1.getCrefonummer().longValue());
        Calendar letzteRechercheP1 = stP1.getLetzteRecherche();

        Archivbestand ab30P2 = parseAB30(pathP2);
        Steuerungsdaten stP2 = ab30P2.getSteuerungsdaten();
        Assert.assertEquals("Crefonummer in den Steuerungsdaten weicht in Phase 2 ab von dem Dateinamen", crefonummer.longValue(), stP2.getCrefonummer().longValue());
        Calendar letzteRechercheP2 = stP2.getLetzteRecherche();

        if (letzteRechercheP1 != null && letzteRechercheP2 != null) {
            int p1CmpP2 = letzteRechercheP1.compareTo(letzteRechercheP2);
            if (p1CmpP2 > 0) {
                List<String> candidateList = mapCandidates.computeIfAbsent("SET_AGE_YEARS/MAX_AGE_YEARS", s -> new ArrayList<>());
                candidateList.add(crefonummer.longValue() + " (" + dateFormat.format(letzteRechercheP1.getTime()) + "->" + dateFormat.format(letzteRechercheP2.getTime()) + ")");
            } else if (p1CmpP2 < 0) {
                List<String> candidateList = mapCandidates.computeIfAbsent("MATCH_Y_M_D_SET_OTHER/MATCH_Y_M_D_RECEIVE_OTHER", s -> new ArrayList<>());
                candidateList.add(crefonummer.longValue() + " (" + dateFormat.format(letzteRechercheP1.getTime()) + "->" + dateFormat.format(letzteRechercheP2.getTime()) + ")");
            }
        }

    }

    private Archivbestand parseAB30(Path p) {
        try (InputStream is = Files.newInputStream(p)) {
            return jaxbUtil.unmarshal(is);
        } catch (IOException | JaxbBasicsMarshallingException e) {
            throw new AssertionError("Parsing des AB30 gescheitert für " + p.toString(), e);
        }
    }


}
