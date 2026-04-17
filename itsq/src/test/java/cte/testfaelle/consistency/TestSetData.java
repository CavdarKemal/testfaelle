package cte.testfaelle.consistency;

import cte.testfaelle.consistency.parser.RefExportsParserIF;
import cte.testfaelle.domain.TestCrefo;
import cte.testfaelle.domain.TestCustomer;
import cte.testfaelle.domain.TestScenario;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static cte.testfaelle.consistency.RefExportsBeschreibung.NOT_SUPPORTED;

public class TestSetData {
    Map<TestSupportClientKonstanten.TEST_PHASE, Map<Long, RefExportsBeschreibung>> refExpTargetMapMap = new TreeMap<>();
    Map<TestSupportClientKonstanten.TEST_PHASE, Map<Long, RohdatenBeschreibung>> rohDatenTargetMapMap = new TreeMap<>();
    final Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> customerTestInfoMapMap;

    public TestSetData(Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> customerTestInfoMapMap) {
        this.customerTestInfoMapMap = customerTestInfoMapMap;
        for (TestSupportClientKonstanten.TEST_PHASE testPhase : TestSupportClientKonstanten.TEST_PHASE.values()) {
            Map<Long, RefExportsBeschreibung> refExportsBeschreibungMap = new TreeMap<>();
            Map<Long, RohdatenBeschreibung> rohDatenBeschreibungMap = new TreeMap<>();
            Map<String, TestCustomer> testCustomerMap = customerTestInfoMapMap.get(testPhase);
            testCustomerMap.keySet().forEach(customerKey -> {
                TestCustomer testCustomer = testCustomerMap.get(customerKey);
                RohdatenParser rohdatenParser = new RohdatenParser();
                RefExportsParserIF refExportsParser = RefExportsConsistencyBase.refExportsParserMap.get(customerKey.toLowerCase(Locale.ROOT));
                if (refExportsParser != null) {
                    testCustomer.getTestScenariosMap().keySet().forEach(scenarioName -> {
                        TestScenario testScenario = testCustomer.getTestScenariosMap().get(scenarioName);
                        Map<String, TestCrefo> testFallNameToTestCrefoMap = testScenario.getTestFallNameToTestCrefoMap();
                        testFallNameToTestCrefoMap.keySet().forEach(testFallName -> {
                            if (!testFallName.startsWith("n")) {
                                TestCrefo testCrefo = testFallNameToTestCrefoMap.get(testFallName);
                                RefExportsBeschreibung refExportsBeschreibung = refExportsParser.parseFile(customerKey, testCrefo);
                                refExportsBeschreibungMap.put(testCrefo.getItsqTestCrefoNr(), refExportsBeschreibung);
                                RohdatenBeschreibung rohdatenBeschreibung = rohdatenParser.parseFile(testCrefo.getItsqAb30XmlFile());
                                rohDatenBeschreibungMap.put(testCrefo.getItsqTestCrefoNr(), rohdatenBeschreibung);
                            }
                        });
                    });
                } else {
                    TimelineLogger.error(getClass(), "\t\t\t\t!!!Die Map enthält keinen Parser für den Kunden!" + customerKey);
                }
            });
            refExpTargetMapMap.put(testPhase, refExportsBeschreibungMap);
            rohDatenTargetMapMap.put(testPhase, rohDatenBeschreibungMap);
        }
    }

    public ConsistencyCheckResult checkRefExportXmlToTestFaelleConsistency() {
        ConsistencyCheckResult consistencyCheckResult = new ConsistencyCheckResult();
        for (TestSupportClientKonstanten.TEST_PHASE testPhase : TestSupportClientKonstanten.TEST_PHASE.values()) {
            Map<String, TestCustomer> testCustomerMapPhaseX = customerTestInfoMapMap.get(testPhase);
            testCustomerMapPhaseX.keySet().forEach(customerKey -> {
                Map<String, TestScenario> testScenariosMap = testCustomerMapPhaseX.get(customerKey).getTestScenariosMap();
                testScenariosMap.keySet().forEach(scenarioName -> {
                    TestScenario testScenario = testScenariosMap.get(scenarioName);
                    List<TestCrefo> testCrefosAsList = testScenario.getTestCrefosAsList();
                    File refExportsDir = testScenario.getItsqRefExportsDir();
                    if (refExportsDir == null || !refExportsDir.exists()) {
                        return;
                    }
                    File[] refXmlFilesAry = refExportsDir.listFiles((dir, name) -> name.endsWith(".xml"));
                    if (refXmlFilesAry == null) {
                        return;
                    }
                    for (File refXmlFile : refXmlFilesAry) {
                        System.out.println("Checke Ref-XML '" + refXmlFile.getAbsolutePath() + "'...");
                        boolean found = false;
                        for(TestCrefo testCrefo : testCrefosAsList) {
                            if(!testCrefo.getTestFallName().startsWith("n")) {
                                File crefoRefXmlFile = testCrefo.getItsqRexExportXmlFile();
                                if (crefoRefXmlFile == null) {
                                    continue;
                                }
                                int compareTo = crefoRefXmlFile.getAbsolutePath().compareTo(refXmlFile.getAbsolutePath());
                                if(compareTo == 0) {
                                    found = true;
                                    break;
                                }
                            }
                        };
                        if(!found) {
                            String errMsg = "!!! Für die Ref-XML-Datei '" + refXmlFile.getAbsolutePath() + "' konnte kein Testfall im Scenario " + testScenario.getCusomerKey() + "::" + testScenario.getScenarioName() + " gefunden werden!";
                            consistencyCheckResult.addAssertion(errMsg);
                        }
                    }
                });
            });
        }
        return consistencyCheckResult;
    }

    public ConsistencyCheckResult checkTestCrefoConsistency() {
        ConsistencyCheckResult consistencyCheckResult = new ConsistencyCheckResult();
        Map<String, TestCustomer> testCustomerMapPhase1 = customerTestInfoMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        Map<String, TestCustomer> testCustomerMapPhase2 = customerTestInfoMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        testCustomerMapPhase1.forEach((testPhase, testCustomerPhase1) -> {
            List<TestCrefo> customersTestCrefosPhase1 = testCustomerPhase1.getAllTestCrefos(false, false);
            TestCustomer testCustomerPhase2 = testCustomerMapPhase2.get(testCustomerPhase1.getCustomerKey());
            customersTestCrefosPhase1.forEach(testCrefoPhase1 -> {
                List<TestCrefo> customersTestCrefosPhase2 = testCustomerPhase2.getAllTestCrefos(false, false);
                TestCrefo testCrefoPhase2 = findTestfallInMap(customersTestCrefosPhase2, testCrefoPhase1.getItsqTestCrefoNr());
                String testFallNamePH1 = testCrefoPhase1.getTestFallName();
                    // In PH1 sind nur P-Fälle erlaubt (Löschsatz-Companion)
                    if (testFallNamePH1.startsWith("n") || testFallNamePH1.startsWith("x")) {
                        String errMsg = "Phase-1 darf nur P-Testfälle enthalten! Gefunden: '" + testFallNamePH1 + "' für Crefo " + testCrefoPhase1.getItsqTestCrefoNr();
                        consistencyCheckResult.addAssertion(errMsg);
                    } else if (testFallNamePH1.startsWith("p")) {
                        // P-Fall in PH1 muss REF-XML und AB30-XML haben
                        if (testCrefoPhase1.getItsqRexExportXmlFile() == null || !testCrefoPhase1.getItsqRexExportXmlFile().exists()) {
                            String errMsg = "Phase-1-P-Testfall für " + testCrefoPhase1.getItsqTestCrefoNr() + " muss eine REF-XML-Datei haben!";
                            consistencyCheckResult.addAssertion(errMsg);
                        }
                        if (testCrefoPhase1.getItsqAb30XmlFile() == null || !testCrefoPhase1.getItsqAb30XmlFile().exists()) {
                            String errMsg = "Phase-1-P-Testfall für " + testCrefoPhase1.getItsqTestCrefoNr() + " muss eine AB30-XML-Datei haben!";
                            consistencyCheckResult.addAssertion(errMsg);
                        }
                        // Zu jedem Phase-1-P-Fall muss ein Phase-2-X-Fall desselben Kunden existieren
                        if (testCrefoPhase2 == null || !testCrefoPhase2.getTestFallName().startsWith("x")) {
                            String errMsg = "Zu Phase-1-P-Testfall " + testCrefoPhase1.getItsqTestCrefoNr() + " fehlt ein X-Testfall in Phase-2 für Kunde " + testCustomerPhase1.getCustomerKey() + "!";
                            consistencyCheckResult.addAssertion(errMsg);
                        }
                    }
            });
        });
        // Phase-2: Regeln für alle Testfall-Typen prüfen
        testCustomerMapPhase2.forEach((customerKey, testCustomerPhase2) -> {
            TestCustomer testCustomerPhase1 = testCustomerMapPhase1.get(customerKey);
            List<TestCrefo> customersTestCrefosPhase2 = testCustomerPhase2.getAllTestCrefos(false, false);
            customersTestCrefosPhase2.forEach(testCrefoPhase2 -> {
                String testFallNamePH2 = testCrefoPhase2.getTestFallName();
                Long crefoNr = testCrefoPhase2.getItsqTestCrefoNr();
                // Regel 2: Jede Crefo muss in ARCHIV-BESTAND/PHASE-2 existieren
                if (testCrefoPhase2.getItsqAb30XmlFile() == null || !testCrefoPhase2.getItsqAb30XmlFile().exists()) {
                    String errMsg = "Phase-2-Testfall " + testFallNamePH2 + " für Crefo " + crefoNr + " (Kunde " + customerKey + ") muss in ARCHIV-BESTAND/PHASE-2 existieren!";
                    consistencyCheckResult.addAssertion(errMsg);
                }
                if (testFallNamePH2.startsWith("p")) {
                    // P-Fall: REF-XML muss existieren
                    if (testCrefoPhase2.getItsqRexExportXmlFile() == null || !testCrefoPhase2.getItsqRexExportXmlFile().exists()) {
                        String errMsg = "Phase-2-P-Testfall für " + crefoNr + " (Kunde " + customerKey + ") muss eine REF-XML-Datei haben!";
                        consistencyCheckResult.addAssertion(errMsg);
                    }
                } else if (testFallNamePH2.startsWith("n")) {
                    // N-Fall: REF-XML darf nicht existieren
                    if (testCrefoPhase2.getItsqRexExportXmlFile() != null && testCrefoPhase2.getItsqRexExportXmlFile().exists()) {
                        String errMsg = "Phase-2-N-Testfall für " + crefoNr + " (Kunde " + customerKey + ") darf keine REF-XML-Datei haben!";
                        consistencyCheckResult.addAssertion(errMsg);
                    }
                } else if (testFallNamePH2.startsWith("x")) {
                    // X-Fall: muss P-Fall in Phase-1 desselben Kunden haben
                    List<TestCrefo> customersTestCrefosPhase1 = testCustomerPhase1 != null
                            ? testCustomerPhase1.getAllTestCrefos(false, false)
                            : new ArrayList<>();
                    TestCrefo testCrefoPhase1 = findTestfallInMap(customersTestCrefosPhase1, crefoNr);
                    if (testCrefoPhase1 == null || !testCrefoPhase1.getTestFallName().startsWith("p")) {
                        String errMsg = "Zu Phase-2-X-Testfall " + crefoNr + " fehlt ein P-Testfall in Phase-1 für Kunde " + customerKey + "!";
                        consistencyCheckResult.addAssertion(errMsg);
                    }
                }
            });
        });
        return consistencyCheckResult;
    }

    public ConsistencyCheckResult checkClzAndExportTypConsistency() {
        ConsistencyCheckResult consistencyCheckResult = new ConsistencyCheckResult();
        Map<Long, RefExportsBeschreibung> refExpTargetMapPhase1 = refExpTargetMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        Map<Long, RefExportsBeschreibung> refExpTargetMapPhase2 = refExpTargetMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        for (Map.Entry<Long, RefExportsBeschreibung> entryPhase1 : refExpTargetMapPhase1.entrySet()) {
            RefExportsBeschreibung refExportsBeschreibungPhase1 = entryPhase1.getValue();
            RefExportsBeschreibung refExportsBeschreibungPhase2 = refExpTargetMapPhase2.get(refExportsBeschreibungPhase1.getCrefoNummer());
            if (refExportsBeschreibungPhase2 != null) {
                // Check CLZs
                if (!refExportsBeschreibungPhase1.getClzString().equals(refExportsBeschreibungPhase2.getClzString())) {
                    String errMsg = "Phase-1-CLZ für " + refExportsBeschreibungPhase1.getCrefoNummer() + " stimmt nicht mit CLZ-Phase2 überein: " + refExportsBeschreibungPhase1.getClzString() + " <> " + refExportsBeschreibungPhase2.getClzString();
                    consistencyCheckResult.addAssertion(errMsg);
                }
                // Check Export-Typs
                if (!refExportsBeschreibungPhase1.getRefExportType().equals(refExportsBeschreibungPhase2.getRefExportType())) {
                    String errMsg = "Phase-1-ExportTyp für die Crefo " + refExportsBeschreibungPhase1.getCrefoNummer() + " stimmt nicht mit ExportTyp-Phase2 überein: " + refExportsBeschreibungPhase1.getClzString() + " <> " + refExportsBeschreibungPhase2.getClzString();
                    consistencyCheckResult.addAssertion(errMsg);
                }
            } else {
                String errMsg = "Zu Phase-1-RefExportsBeschreibung für die Crefo " + refExportsBeschreibungPhase1.getCrefoNummer() + " wurde kein Phase-2-RefExportsBeschreibung gefunden!";
                consistencyCheckResult.addAssertion(errMsg);
            }
        }
        return consistencyCheckResult;
    }

    public ConsistencyCheckResult checkEignerClzConsistency(TestSupportClientKonstanten.TEST_PHASE testPhase) {
        ConsistencyCheckResult consistencyCheckResult = new ConsistencyCheckResult();
        Map<Long, RefExportsBeschreibung> refExpTargetMapForPhase = refExpTargetMapMap.get(testPhase);
        Map<Long, RohdatenBeschreibung> rohDatenTargetMapForPhase = rohDatenTargetMapMap.get(testPhase);
        for (Map.Entry<Long, RefExportsBeschreibung> entry : refExpTargetMapForPhase.entrySet()) {
            RefExportsBeschreibung refExportsBeschreibung = entry.getValue();
            RohdatenBeschreibung rohdatenBeschreibung = rohDatenTargetMapForPhase.get(refExportsBeschreibung.getCrefoNummer());
            if (rohdatenBeschreibung != null) {
                String clzString = refExportsBeschreibung.getClzString();
                if (!NOT_SUPPORTED.equals(clzString)) {
                    long clzEignerVC = rohdatenBeschreibung.getClzEignerVC();
                    if (clzEignerVC != Long.valueOf(clzString).longValue()) {
                        String shortPath = ITSQTestFaelleUtil.getShortPath(refExportsBeschreibung.getRefExportsFile(), TestSupportClientKonstanten.REF_EXPORTS_ROOT);
                        String errMsg = "CLZ in REF-Export-Datei ': " + shortPath + "' " + clzString + "' UNGLEICH CLZ in " + TestSupportClientKonstanten.ARCHIV_BESTAND_ROOT + ": " + clzEignerVC;
                        consistencyCheckResult.addAssertion(errMsg);
                    }
                }
            } else {
                String errMsg = "Zu RefExportsBeschreibung für " + refExportsBeschreibung.getCrefoNummer() + " wurde kein RohdatenBeschreibung in der Map 'rohDatenTargetMap' + ' gefunden!";
                consistencyCheckResult.addAssertion(errMsg);
            }
        }
        return consistencyCheckResult;
    }

    public ConsistencyCheckResult checkArchivBestandPhase1SubsetOfPhase2() {
        ConsistencyCheckResult consistencyCheckResult = new ConsistencyCheckResult();
        Map<String, TestCustomer> phase1Customers = customerTestInfoMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        Map<String, TestCustomer> phase2Customers = customerTestInfoMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        if (phase1Customers.isEmpty() || phase2Customers.isEmpty()) {
            return consistencyCheckResult;
        }
        File phase1Dir = phase1Customers.values().iterator().next().getItsqAB30XmlsDir();
        File phase2Dir = phase2Customers.values().iterator().next().getItsqAB30XmlsDir();
        File[] phase1XmlFiles = phase1Dir.listFiles((dir, name) -> name.endsWith(".xml"));
        if (phase1XmlFiles == null) {
            return consistencyCheckResult;
        }
        for (File phase1File : phase1XmlFiles) {
            File expectedInPhase2 = new File(phase2Dir, phase1File.getName());
            if (!expectedInPhase2.exists()) {
                String errMsg = "ARCHIV-BESTAND/PHASE-1/" + phase1File.getName() + " fehlt in ARCHIV-BESTAND/PHASE-2!";
                consistencyCheckResult.addAssertion(errMsg);
            }
        }
        return consistencyCheckResult;
    }

    private TestCrefo findTestfallInMap(List<TestCrefo> testCrefoList, Long itsqTestCrefoNr) {
        Optional<TestCrefo> optional = testCrefoList.stream().filter(testCrefo -> testCrefo.getItsqTestCrefoNr().equals(itsqTestCrefoNr)).findFirst();
        return optional.orElse(null);
    }

    public static class ConsistencyCheckResult {
        List<String> errMsgList = new ArrayList<>();

        public void addAssertion(String errMsg) {
            errMsgList.add(errMsg);
        }

        public boolean ok() {
            return errMsgList.isEmpty();
        }

        public String getErrors() {
            StringBuilder stringBuilder = new StringBuilder("Folgende Inkonsistenzen sind aufgetreten:");
            errMsgList.forEach(errMsg -> stringBuilder.append("\n\t").append(errMsg));
            return stringBuilder.toString();
        }
    }

}
