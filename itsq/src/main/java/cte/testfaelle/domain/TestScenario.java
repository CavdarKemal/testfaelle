package cte.testfaelle.domain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;

public class TestScenario {

    private boolean activated = true;
    private TestCustomer testCustomer;
    private String scenarioName;
    private File itsqRefExportsDir;
    private File itsqRefExportsPropsFile;

    private final Map<String, TestCrefo> testFallNameToTestCrefoMap = new TreeMap<>();

    public TestScenario(TestCustomer testCustomer, String scenarioName) {
        this.testCustomer = testCustomer;
        this.scenarioName = scenarioName;
        this.itsqRefExportsDir = new File(testCustomer.getItsqRefExportsDir(), scenarioName);
        File[] files = itsqRefExportsDir.listFiles(pathname -> pathname.getName().endsWith(".properties"));
        if (files == null) {
            throw new RuntimeException(String.format("Das Test-Scenario '%s' für den Kunden '%s' enthält keine Properties-Dateien!\nDer Pfad ist '%s'!", scenarioName, testCustomer.getCustomerName(), itsqRefExportsDir.getAbsolutePath()));
        }
        if (files.length != 1) {
            throw new RuntimeException(String.format("Das Test-Scenario '%s' für den Kunden '%s' enthält %d Properties-Dateien\nErlaubt ist genau eine Properties-Datei!", scenarioName, testCustomer.getCustomerName(), files.length));
        }
        this.itsqRefExportsPropsFile = new File(itsqRefExportsDir, files[0].getName());
        initTestCrefosMapFromItsqRefExpPropsFile();
    }

    public TestScenario(TestScenario toBeCloned) {
        setActivated(toBeCloned.isActivated());
        setScenarioName(toBeCloned.getScenarioName());
        setTestCustomer(toBeCloned.getTestCustomer());
        setItsqRefExportsDir(toBeCloned.getItsqRefExportsDir());
        setItsqRefExportsPropsFile(toBeCloned.getItsqRefExportsPropsFile());
        testFallNameToTestCrefoMap.putAll(toBeCloned.getTestFallNameToTestCrefoMap());
    }

    @Override
    public String toString() {
        return scenarioName + " #" + testFallNameToTestCrefoMap.size();
    }

    public TestCustomer getTestCustomer() {
        return testCustomer;
    }

    public void setTestCustomer(TestCustomer testCustomer) {
        this.testCustomer = testCustomer;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getCusomerKey() {
        return testCustomer.getCustomerKey();
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public List<TestCrefo> getTestCrefosAsList() {
        return new ArrayList(getTestFallNameToTestCrefoMap().values());
    }

    public void setTestCrefosAsList(List<TestCrefo> testCrefosAsList) {
        getTestFallNameToTestCrefoMap().clear();
        testCrefosAsList.forEach(testCrefo -> getTestFallNameToTestCrefoMap().put(testCrefo.getTestFallName(), testCrefo));
    }

    public Map<String, TestCrefo> getTestFallNameToTestCrefoMap() {
        return testFallNameToTestCrefoMap;
    }

    /*******************     ItsqRefExports  *************************/
    public File getItsqRefExportsDir() {
        return itsqRefExportsDir;
    }

    public void setItsqRefExportsDir(File itsqRefExportsDir) {
        this.itsqRefExportsDir = itsqRefExportsDir;
    }

    public File getItsqRefExportsPropsFile() {
        return itsqRefExportsPropsFile;
    }

    public void setItsqRefExportsPropsFile(File itsqRefExportsPropsFile) {
        this.itsqRefExportsPropsFile = itsqRefExportsPropsFile;
    }

    protected void initTestCrefosMapFromItsqRefExpPropsFile() {
        try {
            List<File> refExportXmlFileList = Arrays.stream(itsqRefExportsDir.listFiles((dir, name) -> name.endsWith(".xml"))).collect(Collectors.toList());
            List<String> propsFileContent = FileUtils.readLines(itsqRefExportsPropsFile);
            propsFileContent.forEach(line -> {
                if (!line.isBlank() && !line.startsWith("#")) {
                    String[] splitEqual = line.split("=");
                    try {
                        String testFallName = splitEqual[0].trim();
                        boolean shouldBeExported = !testFallName.startsWith("n");
                        final String[] splitHash = splitEqual[1].trim().split("#");
                        long crefoNr = Long.parseLong(splitHash[0].trim());
                        String testFallInfo = (splitHash.length > 1) ? splitHash[1] : "";
                        TestCrefo testCrefo = new TestCrefo(testFallName, crefoNr, testFallInfo, shouldBeExported);

                        File itsqRefExpXmlFile = findXmlFileForCrefo(refExportXmlFileList, crefoNr);
                        File itsqAb30XmlFile = new File(getTestCustomer().getItsqAB30XmlsDir(), (crefoNr + ".xml"));
                        if (!shouldBeExported && (itsqRefExpXmlFile != null && itsqRefExpXmlFile.exists())) {
                            String errorStr = "Für die Test-Crefo '" + testFallName + "':" + crefoNr + " dürfte es KEINE RefExport-XML existieren!";
                            TimelineLogger.warn(getClass(), errorStr);
                        } else if (shouldBeExported && (itsqRefExpXmlFile == null || !itsqRefExpXmlFile.exists())) {
                            String errorStr = "Für die Test-Crefo '" + testFallName + "':" + crefoNr + " müsste es EINE RefExport-XML existieren!";
                            TimelineLogger.warn(getClass(), errorStr);
                        }
                        testCrefo.setItsqRexExportXmlFile(itsqRefExpXmlFile);
                        if (itsqAb30XmlFile.exists()) {
                            testCrefo.setItsqAb30XmlFile(itsqAb30XmlFile);
                        }
                        testFallNameToTestCrefoMap.put(testFallName, testCrefo);
                    } catch (Exception ex) {
                        String errorStr = "\n!!! Exception in der Zeile '" + line + "' der Datei '" + itsqRefExportsPropsFile.getName() + "':\n" + ex.getMessage();
                        TimelineLogger.error(getClass(), errorStr);
                    }
                }
            });
        } catch (IOException ex) {
            String errorStr = "\n!!! Exception beim Lesen der Properties-Datei '" + itsqRefExportsPropsFile.getAbsolutePath() + "'!\n" + ex.getMessage();
            TimelineLogger.error(getClass(), errorStr);
        }
    }

    private File findXmlFileForCrefo(Collection<File> allXmlFiles, long crefoNr) {
        List<File> collect = allXmlFiles.stream().filter(theFile -> theFile.getName().contains(crefoNr + "")).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }

    private File findXmlFileForTestfallAndCrefo(Collection<File> allXmlFiles, String testFallName, long crefoNr) {
        List<File> collect = allXmlFiles.stream().filter(theFile -> theFile.getName().contains(testFallName) && theFile.getName().contains(crefoNr + "")).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }

    public StringBuilder dump(String prefix) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix + "\tScenario: " + scenarioName);
        stringBuilder.append(prefix + "\t\tTest-Crefos");
        stringBuilder.append(prefix + "\t\t\ttestFallName\titsqTestCrefoNr\tpseudoCrefoNr\titsqPhase2XmlFile\titsqRexExportXmlFile\tpseudoRefExportXmlFile\tcollectedXmlFile\trestoredXmlFile");
        testFallNameToTestCrefoMap.entrySet().forEach(testCrefoEntry -> {
            TestCrefo testCrefo = testCrefoEntry.getValue();
            stringBuilder.append(testCrefo.dump(prefix + "\t\t\t"));
        });
        return stringBuilder;
    }

}
