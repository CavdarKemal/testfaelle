package cte.testfaelle.automaticupdates;

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test-Klasse für {@link UpdInkassoaktenPlanIst}
 */
public class UpdInkassoaktenPlanIstTest {

    @Test
    public void testGenerateRandom() {
        UpdInkassoaktenPlanIst cut = new UpdInkassoaktenPlanIst();
        for (int i = 0; i < 1000; i++) {
            double generatedRandom = cut.nextRandomFactor();
            Assert.assertTrue(UpdInkassoaktenPlanIst.MIN_FAKTOR <= generatedRandom);
            Assert.assertTrue(UpdInkassoaktenPlanIst.MAX_FAKTOR >= generatedRandom);
        }
    }

    @Test
    public void testMultiply() {
        UpdInkassoaktenPlanIst cut = new UpdInkassoaktenPlanIst();
        Assert.assertEquals("75.00", cut.multipy(BigDecimal.valueOf(100L), 0.75).toString());
        Assert.assertEquals("75.00", cut.multipy(BigDecimal.valueOf(100L), 0.75005).toString());
        Assert.assertEquals("765.43", cut.multipy(BigDecimal.valueOf(1000L), 0.7654321).toString());
    }

}
