package cte.testfaelle;


import cte.testfaelle.domain.TestCrefo;
import cte.testfaelle.domain.TestCustomer;
import cte.testfaelle.domain.TestScenario;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ITSQTestFaelleUtilTest {
    public static final File TEST_SET_DIR = new File(new File(System.getProperty("user.dir")).getParentFile(), "test_set");
    ITSQTestFaelleUtil cut;

    @Before
    public void setUp() throws Exception {
        cut = new ITSQTestFaelleUtil(TEST_SET_DIR);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void getGetTestCustomerC01() {
        TimelineLogger.info(getClass(), "================== getGetTestCustomer co1 ==================");
        final TestCustomer testCustomer = cut.getTestCustomer("C01", TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        checkTestCustomer(testCustomer);
        TimelineLogger.info(getClass(), "------------------ getGetTestCustomer C01 ------------------");
    }

    @Test
    public void getTestCustomerList() {
        TimelineLogger.info(getClass(), "================== getTestCustomerList ALL ==================");
        final List<TestCustomer> testCustomerList = cut.getTestCustomerList(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        Assert.assertNotNull(testCustomerList);
        for (TestCustomer testCustomer : testCustomerList) {
            checkTestCustomer(testCustomer);
        }
        TimelineLogger.info(getClass(), "------------------ getTestCustomerList ALL ------------------");
    }

    private void checkTestCustomer(TestCustomer testCustomer) {
        Assert.assertNotNull(testCustomer);
        final List<TestScenario> testScenariosList = testCustomer.getTestScenariosList();
        Assert.assertNotNull("Kunde '" + testCustomer.getCustomerKey() + "': Scenarios nicht gesetzt!", testScenariosList);
        for (TestScenario testScenario : testScenariosList) {
            Assert.assertEquals("Kunde '" + testCustomer.getCustomerKey() + "':: Dem Scenario " + testScenario.getScenarioName() + " ist falscher TestCustomer '" + testCustomer.getCustomerKey() + "' zugewiesen!", testCustomer, testScenario.getTestCustomer());
            Assert.assertNotNull("Kunde '" + testCustomer.getCustomerKey() + "':: PropertiesFile des Scenario ist nicht gesetzt!", testScenario.getItsqRefExportsPropsFile());
            Assert.assertNotNull("Kunde '" + testCustomer.getCustomerKey() + "' Name des Scenario ist nicht gesetzt!", testScenario.getScenarioName());
            boolean notNegativAndEmpty = !testScenario.getScenarioName().contains("Negativ") && testScenario.getTestCrefosAsList().isEmpty();
            Assert.assertFalse("Kunde '" + testCustomer.getCustomerKey() + "':: Scenario '" + testScenario.getScenarioName() + "' enthält keine Test-Crefos!", notNegativAndEmpty);
            if (!testScenario.getTestCrefosAsList().isEmpty()) {
                checkFilesList(testScenario);
            }
            if (!testScenario.getTestCrefosAsList().isEmpty()) {
                final TestCrefo testCrefo = testScenario.getTestCrefosAsList().get(0);
                Assert.assertNotNull("Kunde: '" + testCustomer.getCustomerKey() + "':: Test-Crefo der Test-Crefo-List des Scenario '" + testScenario.getScenarioName() + "' nicht gesetzt!", testCrefo);
                Assert.assertNotNull("Kunde: '" + testCustomer.getCustomerKey() + "':: Crefonummer der Test-Crefo nicht gesetzt!", testCrefo.getItsqTestCrefoNr());
                Assert.assertNotNull("Kunde: '" + testCustomer.getCustomerKey() + "':: TetfallName der Test-Crefo im Scenario '" + testScenario.getScenarioName() + "' nicht gesetzt!", testCrefo.getTestFallName());
                if (testCrefo.getTestFallName().startsWith("n")) {
                    Assert.assertNull("Kunde: '" + testCustomer.getCustomerKey() + "':: Negativ-Testfall im Scenario '" + testScenario.getScenarioName() + "' dürfte keine Ref-XML-Datei haben!", testCrefo.getItsqRexExportXmlFile());
                } else {
                    Assert.assertNotNull("Kunde: '" + testCustomer.getCustomerKey() + "':: Ref-Export-Dateiname der Test-Crefo im Scenario '" + testScenario.getScenarioName() + "' nicht gesetzt!", testCrefo.getItsqRexExportXmlFile());
                }
            }
        }
    }

    private void checkFilesList(TestScenario testScenario) {
        for (TestCrefo testCrefo : testScenario.getTestCrefosAsList()) {
            boolean schouldExist = !testCrefo.getTestFallName().startsWith("n");
            File refExportFile = testCrefo.getItsqRexExportXmlFile();
            if (!schouldExist && (refExportFile != null && refExportFile.exists())) {
                Assert.fail("!!! Für die Test-Crefo " + testCrefo.getTestFallName() + " dürfte es keine RefExport-XML existieren!");
            } else if (schouldExist && (refExportFile == null || !refExportFile.exists())) {
                Assert.fail("!!! Für die Test-Crefo " + testCrefo.getTestFallName() + " müsste es eine RefExport-XML existieren!");
            }
        }
    }
}