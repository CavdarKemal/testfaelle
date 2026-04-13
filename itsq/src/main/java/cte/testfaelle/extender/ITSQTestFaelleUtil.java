package cte.testfaelle.extender;

import cte.testfaelle.domain.TestCustomer;
import cte.testfaelle.domain.TestScenario;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ITSQTestFaelleUtil {

    private final File refExportXmlsDir;
    File phase1ArchivBestandDir;
    File phase2ArchivBestandDir;
    private Map<String, TestCustomer> testCustomerMap;

    public ITSQTestFaelleUtil(File testSetDir) throws Exception {
        this.refExportXmlsDir = new File(testSetDir, TestSupportClientKonstanten.REF_EXPORTS_ROOT);
        this.phase1ArchivBestandDir = new File(testSetDir, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1);
        this.phase2ArchivBestandDir = new File(testSetDir, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2);
    }

    public static Map<Long, Path> scanSourceDirectory(File srcDir) throws IOException {
        Map<Long, Path> scanResult = new LinkedHashMap<>();
        Path srcPath = Paths.get(srcDir.getAbsolutePath());
        try (Stream<Path> directoryContentStream = Files.list(srcPath)) {
            directoryContentStream
                    .filter(p -> TestSupportClientKonstanten.CRF_XML_PATTERN.matcher(p.getFileName().toString()).matches())
                    .forEach(p -> {
                        Long crefonummer = parseCrefo(p.getFileName().toString().substring(0, 10));
                        if (crefonummer != null) {
                            scanResult.put(crefonummer, p);
                        }
                    });
        }
        return scanResult;
    }

    public static Long parseCrefo(String string) {
        try {
            long crf = Long.parseLong(string);
            if (crf < 1000000000L || crf > 9999999999L) {
                return null;
            }
            return crf;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getShortPath(File theFile, String pathPrefix) {
        String absolutePath = theFile.getAbsolutePath();
        if (pathPrefix.startsWith("./")) {
            pathPrefix = pathPrefix.substring(2);
        }
        return absolutePath.substring(absolutePath.indexOf(pathPrefix));
    }

    public List<TestCustomer> getTestCustomerList(TestSupportClientKonstanten.TEST_PHASE testPhase) {
        if (testCustomerMap == null) {
            testCustomerMap = getCustomerTestInfoMap(testPhase);
        }
        return new ArrayList<>(testCustomerMap.values());
    }

    public TestCustomer getTestCustomer(String customerKey, TestSupportClientKonstanten.TEST_PHASE testPhase) {
        if (testCustomerMap == null) {
            testCustomerMap = getCustomerTestInfoMap(testPhase);
        }
        return testCustomerMap.get(customerKey);
    }

    public Map<String, TestCustomer> getCustomerTestInfoMap(TestSupportClientKonstanten.TEST_PHASE testPhase) {
        TimelineLogger.info(getClass(), "Initialisiere Kunden für " + testPhase.getDirName());
        Map<String, TestCustomer> customerTestInfoMap = new TreeMap<>();
        File refExportXmlsDirPhX = new File(refExportXmlsDir, testPhase.getDirName());
        List<File> customerDirsList = Arrays.stream(refExportXmlsDirPhX.listFiles((dir, name) -> dir.isDirectory())).collect(Collectors.toList());
        for (File customerDir : customerDirsList) {
            String customerKey = customerDir.getName().toUpperCase(Locale.ROOT);
            TimelineLogger.info(getClass(), "\tInitialisiere Kunde " + customerKey);
            TestCustomer testCustomer = new TestCustomer(customerKey, customerKey);
            testCustomer.setItsqAB30XmlsDir(testPhase.equals(TestSupportClientKonstanten.TEST_PHASE.PHASE_1) ? phase1ArchivBestandDir : phase2ArchivBestandDir);
            testCustomer.setItsqRefExportsDir(customerDir);
            testCustomer.setTestPhase(testPhase);
            customerTestInfoMap.put(customerKey, testCustomer);
            List<File> scenariDirsList = Arrays.stream(customerDir.listFiles((dir, name) -> dir.isDirectory())).collect(Collectors.toList());
            scenariDirsList.forEach(scenariDir -> {
                TimelineLogger.info(getClass(), "\t\tInitialisiere Scenario " + scenariDir.getName());
                TestScenario testScenario = new TestScenario(testCustomer, scenariDir.getName());
                testCustomer.addTestScenario(testScenario);
            });
        }
        return customerTestInfoMap;
    }

    public Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> getCustomerTestInfoMapMap() {
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> customerTestInfoMapMap = new HashMap<>();
        for (TestSupportClientKonstanten.TEST_PHASE testPhase : TestSupportClientKonstanten.TEST_PHASE.values()) {
            TimelineLogger.info(getClass(), "ITSQTestFaelleUtil#getCustomerTestInfoMapMap:: TestCustomer-Map für die Phase " + testPhase.name() + " wird gebaut...");
            Map<String, TestCustomer> customerTestInfoMap = getCustomerTestInfoMap(testPhase);
            customerTestInfoMapMap.put(testPhase, customerTestInfoMap);
        }
        return customerTestInfoMapMap;
    }
}
