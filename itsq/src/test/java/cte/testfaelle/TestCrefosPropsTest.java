package cte.testfaelle;


import cte.testfaelle.domain.TestCustomer;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCrefosPropsTest {
    ITSQTestFaelleUtil cut;

    @Before
    public void setUp() throws Exception {
        cut = new ITSQTestFaelleUtil(ITSQTestFaelleUtilTest.TEST_SET_DIR);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPropertiesConsistence() throws Exception {
        TimelineLogger.info(getClass(), "================== testPropertiesConsistence ==================");
        List<TestCustomer> testCustomerListPhase1 = cut.getTestCustomerList(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        testCustomerListPhase1.stream().forEach(testCustomer -> {
            TimelineLogger.info(getClass(), "Phase 1: Aktueller Kunde " + testCustomer.getCustomerKey());

        });
        List<TestCustomer> testCustomerListPhase2 = cut.getTestCustomerList(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        testCustomerListPhase2.stream().forEach(testCustomer -> {
            TimelineLogger.info(getClass(), "Phase 2: Aktueller Kunde " + testCustomer.getCustomerKey());

        });
        TimelineLogger.info(getClass(), "------------------ testPropertiesConsistence ------------------");
    }

}