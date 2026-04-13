package cte.testfaelle.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCustomer {
    private String customerName;
    private String customerKey;
    private TestSupportClientKonstanten.TEST_PHASE testPhase;
    private File itsqRefExportsDir;
    private File itsqAB30XmlsDir;
    private final Map<String, TestScenario> testScenariosMap = new HashMap<>();

    public TestCustomer(String customerKey, File itsqAB30XmlsDir, File itsqRefExportsDir) {
        this.customerKey = customerKey;
        this.itsqAB30XmlsDir = itsqAB30XmlsDir;
        this.itsqRefExportsDir = itsqRefExportsDir;
    }

    public TestCustomer(String customerKey, String customerName) {
        this.customerKey = customerKey;
        this.customerName = customerName;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public String getCustomerName() {
        return customerName;
    }

    public TestSupportClientKonstanten.TEST_PHASE getTestPhase() {
        return testPhase;
    }

    public void setTestPhase(TestSupportClientKonstanten.TEST_PHASE testPhase) {
        this.testPhase = testPhase;
    }

    public File getItsqRefExportsDir() {
        return itsqRefExportsDir;
    }

    public void setItsqRefExportsDir(File itsqRefExportsDir) {
        this.itsqRefExportsDir = itsqRefExportsDir;
    }

    public File getItsqAB30XmlsDir() {
        return itsqAB30XmlsDir;
    }

    public void setItsqAB30XmlsDir(File itsqAB30XmlsDir) {
        this.itsqAB30XmlsDir = itsqAB30XmlsDir;
    }

    public void addTestScenario(TestScenario testScenario) {
        testScenariosMap.put(testScenario.getScenarioName(), testScenario);
    }

    public Map<String, TestScenario> getTestScenariosMap() {
        return testScenariosMap;
    }

    public List<TestScenario> getTestScenariosList() {
        List<TestScenario> theList = new ArrayList<>(testScenariosMap.values());
        return theList;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", customerKey, customerName);
    }

    public List<TestCrefo> getAllTestCrefos(boolean activeOnly, boolean positiveOnly) {
        List<TestCrefo> testCrefosList = new ArrayList<>();
        final List<TestScenario> testScenariosList = getTestScenariosList();
        for (TestScenario testScenario : testScenariosList) {
            if (!activeOnly || testScenario.isActivated()) {
                Map<String, TestCrefo> testFallNameToTestCrefoMap = testScenario.getTestFallNameToTestCrefoMap();
                testFallNameToTestCrefoMap.entrySet().forEach(testCrefoEntry -> {
                    TestCrefo testCrefo = testCrefoEntry.getValue();
                    if (!activeOnly || testCrefo.isActivated()) {
                        if (!positiveOnly || testCrefo.isShouldBeExported()) {
                            testCrefosList.add(testCrefo);
                        }
                    }
                });
            }
        }
        return testCrefosList;
    }

}
