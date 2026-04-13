package cte.testfaelle;

import cte.testfaelle.domain.DateTimeFinderFunction;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class DateTimeFinderFunctionTest {

    @Rule
    public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();

    @Test
    public void testForXmlTags() {
        DateTimeFinderFunction cut = new DateTimeFinderFunction(Arrays.asList("vorraetig-bis", "letzte-archivaenderung", "jahr", "monat", "tag"));

        Boolean found = cut.apply("erfassung", "2001-01-11+01:00");
        Assert.assertFalse("Für das XML-Tag 'erfassung' sollte kein Treffer existieren, da nicht in der XMLTag-Liste! ", found);

        found = cut.apply("vorraetig-bis", "2020-03-13+01:00");
        Assert.assertTrue("Für das XML-Tag 'vorraetig-bis' sollte ein Treffer existieren, da das Tag in der XMLTag-Liste ist.", found);

        found = cut.apply("letzte-archivaenderung", "2020-03-19T09:53:24.852+01:00");
        Assert.assertTrue("Für das XML-Tag 'letzte-archivaenderung' sollte ein Treffer existieren, da das Tag in der XMLTag-Liste ist.", found);

        found = cut.apply("jahr", "2020");
        Assert.assertTrue("Für das XML-Tag 'jahr' sollte ein Treffer existieren, da das Tag in der XMLTag-Liste ist.", found);

        found = cut.apply("monat", "11");
        Assert.assertTrue("Für das XML-Tag 'monat' sollte ein Treffer existieren, da das Tag in der XMLTag-Liste ist.", found);

        found = cut.apply("tag", "23");
        Assert.assertTrue("Für das XML-Tag 'tag' sollte ein Treffer existieren, da das Tag in der XMLTag-Liste ist.", found);

    }
}
