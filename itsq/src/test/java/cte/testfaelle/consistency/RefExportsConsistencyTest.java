package cte.testfaelle.consistency;

import cte.testfaelle.ITSQTestFaelleUtilTest;
import cte.testfaelle.domain.TestCustomer;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test-Klasse zur Überprüfung der Konsistenz innerhalb der ARCHIV_BESTAND- und REF-EXPORTS-Dateien
 */
public class RefExportsConsistencyTest {
    TestSetData testSetData;

    @Before
    public void setUp() throws Exception {
        if (testSetData == null) {
            ITSQTestFaelleUtil testFaelleUtil = new ITSQTestFaelleUtil(ITSQTestFaelleUtilTest.TEST_SET_DIR);
            Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> customerTestInfoMapMap = testFaelleUtil.getCustomerTestInfoMapMap();
            testSetData = new TestSetData(customerTestInfoMapMap);
        }
    }

    @Test
    public void testCheckTestCrefoConsistency() {
        TestSetData.ConsistencyCheckResult consistencyCheckResult = testSetData.checkTestCrefoConsistency();
        Assert.assertTrue(consistencyCheckResult.getErrors(), consistencyCheckResult.ok());
    }

    @Test
    public void testRefExportXmlToTestFaelleConsistency() {
        TestSetData.ConsistencyCheckResult consistencyCheckResult = testSetData.checkRefExportXmlToTestFaelleConsistency();
        Assert.assertTrue(consistencyCheckResult.getErrors(), consistencyCheckResult.ok());
    }

    @Test
    public void testCheckClzAndExportTypConsistency() {
        TestSetData.ConsistencyCheckResult consistencyCheckResult = testSetData.checkClzAndExportTypConsistency();
        Assert.assertTrue(consistencyCheckResult.getErrors(), consistencyCheckResult.ok());
    }

    @Test
    public void testCheckEignerClzConsistencyPhase1() {
        TestSetData.ConsistencyCheckResult consistencyCheckResult = testSetData.checkEignerClzConsistency(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        Assert.assertTrue(consistencyCheckResult.getErrors(), consistencyCheckResult.ok());
    }

    @Test
    public void testCheckEignerClzConsistencyPhase2() {
        TestSetData.ConsistencyCheckResult consistencyCheckResult = testSetData.checkEignerClzConsistency(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        Assert.assertTrue(consistencyCheckResult.getErrors(), consistencyCheckResult.ok());
    }

    @Test
    public void testCheckArchivBestandPhase1SubsetOfPhase2() {
        TestSetData.ConsistencyCheckResult consistencyCheckResult = testSetData.checkArchivBestandPhase1SubsetOfPhase2();
        Assert.assertTrue(consistencyCheckResult.getErrors(), consistencyCheckResult.ok());
    }

}
