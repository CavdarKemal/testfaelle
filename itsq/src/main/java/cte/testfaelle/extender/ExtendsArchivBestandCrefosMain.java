package cte.testfaelle.extender;

import cte.testfaelle.domain.TestCustomer;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import java.io.File;
import java.util.Map;

public class ExtendsArchivBestandCrefosMain {
    public static final File TEST_SET_DIR = new File(new File(System.getProperty("user.dir")), "test_set");

    public static void main(String[] args) throws Exception {
        File workDir = new File(System.getProperty("user.dir"));
        if (!TimelineLogger.configure(workDir, "ExtendAB30.log", "ExtendAB30-Actions.log")) {
            throw new RuntimeException("Exception beim Konfigurieren der LOG-Dateien!\n");
        }
        ITSQTestFaelleUtil testFaelleUtil = new ITSQTestFaelleUtil(TEST_SET_DIR);
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> customerTestInfoMapMap = testFaelleUtil.getCustomerTestInfoMapMap();
        ExtendArchivBestandCrefos cut = new ExtendArchivBestandCrefos(customerTestInfoMapMap, TEST_SET_DIR);
        cut.extendTestCrefos();
        TimelineLogger.close();
    }
}
