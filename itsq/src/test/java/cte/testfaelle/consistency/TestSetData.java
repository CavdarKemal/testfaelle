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
                if(testCrefoPhase2 != null) {
                    String testFallNamePH1 = testCrefoPhase1.getTestFallName();
                    String testFallNamePH2 = testCrefoPhase2.getTestFallName();
                    // Ein X-Fall in PH2 muss ein P-Fall in PH1 sein!
                    if (testFallNamePH2.startsWith("x") && !testFallNamePH1.startsWith("p")) {
                        String errMsg = "Phase-1-TestfallTyp für " + testCrefoPhase1.getItsqTestCrefoNr() + " muss 'P' sein, da Phase-2-TestfallTyp 'X' ist!";
                        consistencyCheckResult.addAssertion(errMsg);
                    }
                    if (testFallNamePH1.startsWith("p")) {
                        // Für ein P-Fall in PH1 müssen sowohl REF-XML-File als auch AB30-XML-Datei existieren
                        File refXmlFile = testCrefoPhase1.getItsqRexExportXmlFile();
                        if(refXmlFile == null || !refXmlFile.exists()) {
                            String errMsg = "Phase-1-P-Testfall für " + testCrefoPhase1.getItsqTestCrefoNr() + " muss ein REF-XML-Datei haben!";
                            consistencyCheckResult.addAssertion(errMsg);
                        }
                        File ab30XmlFile = testCrefoPhase1.getItsqAb30XmlFile();
                        if(ab30XmlFile == null || !ab30XmlFile.exists()) {
                            String errMsg = "Phase-1-P-Testfall für " + testCrefoPhase1.getItsqTestCrefoNr() + " muss ein AB30-XML-Datei haben!";
                            consistencyCheckResult.addAssertion(errMsg);
                        }
                    }
                    // N-Fälle brauchen keine REF-XML oder AB30-XML Prüfung,
                    // da eine Crefo szenarioübergreifend verschiedene Testfall-Typen (P/N/X) haben kann
                    // In PH1 darf es kein X-Fall geben
                    if (testFallNamePH1.startsWith("x")) {
                        String errMsg = "In Phase-1 darf es kein X-Testfall gegen!";
                        consistencyCheckResult.addAssertion(errMsg);
                    }
                }
                else {
                    String errMsg = "Phase-1-TestCrefo für " + testCrefoPhase1.getItsqTestCrefoNr() + " existiert nicht in Pahase-2-Map!";
                    consistencyCheckResult.addAssertion(errMsg);
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
