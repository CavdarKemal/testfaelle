package cte.testfaelle.rohdatenverwendung;

import cte.testfaelle.ITSQTestFaelleUtilTest;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Im Rahmen der Einführung automatischer Änderungen an den Rohdaten soll geprüft werden, welche
 * Test-Crefos wie oft referenziert werden. Besonders interessant sind natürlich die Fälle, die gar nicht
 * oder genau ein mal genutzt werden.
 */
public class RohdatenVerwendungTest {

    protected static final Pattern CREFO_FILE_PATTERN = Pattern.compile("(\\d){10}.xml");
    @Rule
    public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();

    private Map<String, UsageOfCrefo> getUsages() throws IOException {
        // Leere Map mit allen Crefos anlegen...
        Map<String, UsageOfCrefo> usages = new TreeMap<>();
        populateEmptyMap(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1), usages, UsageOfCrefo::setAvailableInP1);
        populateEmptyMap(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2), usages, UsageOfCrefo::setAvailableInP2);
        // Testfall-Beschreibungen (rekursiv) abarbeiten
        populateUsages(usages, "", new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.REF_EXPORTS_PHASE_2));
        return usages;
    }

    private void populateEmptyMap(File srcDir, Map<String, UsageOfCrefo> targetMap, Consumer<UsageOfCrefo> consumer) {
        Assert.assertTrue("Quell-Verzeichnis existiert nicht: " + srcDir.getName(), srcDir.exists());
        Assert.assertTrue(srcDir.isDirectory());
        for (File f : srcDir.listFiles()) {
            if (f.isFile() && CREFO_FILE_PATTERN.matcher(f.getName()).matches()) {
                String mapKey = f.getName().substring(0, f.getName().length() - 4);
                UsageOfCrefo uoc = targetMap.computeIfAbsent(mapKey, UsageOfCrefo::new);
                consumer.accept(uoc);
            }
        }
    }

    private void populateUsages(Map<String, UsageOfCrefo> targetMap, String path, File parentDir) throws IOException {
        // Plausi-Checks...
        Assert.assertTrue(parentDir.exists());
        Assert.assertTrue(parentDir.isDirectory());
        // Kunden-Kürzel...
        int firstSlash = path.indexOf('/');
        final String kundenKuerzel = (firstSlash < 0) ? "" : path.substring(0, firstSlash).toUpperCase();

        for (File f : parentDir.listFiles()) {
            if (f.getName().startsWith(".") || f.getName().endsWith(".cfg")) {
                continue;
            }
            if (f.isDirectory()) {
                populateUsages(targetMap, path + "/" + f.getName(), f);
            } else if (f.isFile() && f.getName().endsWith(".properties")) {
                Properties properties = new Properties();
                try (FileInputStream fis = new FileInputStream(f);
                     InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)
                ) {
                    String propertyFileName = f.getName();
                    properties.load(isr);
                    for (Map.Entry<Object, Object> e : properties.entrySet()) {
                        String scenarioName = e.getKey().toString();
                        String value = e.getValue().toString();
                        int posComment = value.indexOf('#');
                        if (posComment >= 0) {
                            value = value.substring(0, posComment);
                        }
                        value = value.trim();
                        UsageOfCrefo container = targetMap.get(value);
                        if (container == null) {
                            Assert.fail("kein Eintrag in der Ziel-Map für: " + value);
                        }
                        container.addUsedInScenario(new UsedInTestScenario(kundenKuerzel, path, propertyFileName, scenarioName));
                    }
                }
            }
        }
    }

    @Test
    public void testScanUsages() throws IOException {
        Map<String, UsageOfCrefo> usages = getUsages();
        Assert.assertNotNull(usages);
        Assert.assertFalse(usages.isEmpty());
        for (Map.Entry<String, UsageOfCrefo> entry : usages.entrySet()) {
            int n = entry.getValue().getNumberOfScenarios();
            if (n == 0) {
                TimelineLogger.warn(getClass(), "Crefo {} wird in keinem einzigen Test-Szenario direkt genutzt", entry.getKey());
            } else if (n == 1) {
                TimelineLogger.info(getClass(), "Crefo {} wird in genau einem Test-Szenario direkt genutzt", entry.getKey());
            } else {
                Set<String> beiKunden = new TreeSet<>();
                for (UsedInTestScenario ut : entry.getValue().getUsedInScenarios()) {
                    beiKunden.add(ut.getKundenKuerzel());
                }
                if (beiKunden.size() == 0) {
                    TimelineLogger.info(getClass(), "Crefo {} wird in mehreren Test-Szenarien aber nur bei einem Kunden {} direkt genutzt", entry.getKey(), beiKunden);
                }
            }
        }
    }

}

