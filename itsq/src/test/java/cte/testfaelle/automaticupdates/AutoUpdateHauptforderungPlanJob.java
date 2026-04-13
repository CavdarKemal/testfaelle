package cte.testfaelle.automaticupdates;

import cte.testfaelle.ITSQTestFaelleUtilTest;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import java.io.File;
import org.junit.Test;

/**
 * Utility-Klasse zum automaatischen Befüllen von 'Hauptforderung-Plan' in den Testcrefos
 */
public class AutoUpdateHauptforderungPlanJob extends AutoUpdateJobAbstract {

    public AutoUpdateHauptforderungPlanJob() {
        super();
    }

    @Test
    public void autoUpdateHauptforderungPlan() throws Exception {
        UpdInkassoaktenPlanIst updateFunction = new UpdInkassoaktenPlanIst();
        autoUpdate(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.REF_EXPORTS_PHASE_2), updateFunction);
    }

}
