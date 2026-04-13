package cte.testfaelle;

import cte.testfaelle.domain.DateTimeModifierFunction;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class DateTimeModifierFunctionTest {
    @Rule
    public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();

    @Test
    public void testForAmount() throws ParseException {
        String strFileDate = "2020-10-21+01:00";
        String newYearStr = doTestWithForAmount("jahr", "2017", strFileDate);
        String xmlTagValue = "2017-06-16";
        TimelineLogger.info(getClass(), "Datei-Datum: " + strFileDate + ", Alter-Feld-Wert: " + xmlTagValue + ", Neuer-Feld-Wert: " + newYearStr);
    }

    @Test
    public void testForDATE_FORMAT_YYYY_MM_DD() throws ParseException {
        doTestWithDateFormat("2021-01-01+01:00", "2020-01-01+01:00");
    }

    @Test
    public void testForDATE_FORMAT_YYYY_MM_DD_HH_MM_SS() throws ParseException {
        doTestWithDateFormat("2020-11-01T09:53:24.852+01:00", "2018-05-01+01:00");
    }

    @Test
    public void testForDATE_FORMAT_YYYY_MM_DD_HH_MM_SSx() throws ParseException {
        doTestWithDateFormat("2020-01-01T09:53:24+01:00", "2011-05-01+01:00");
    }

    private String doTestWithForAmount(String xmlTagName, String xmlTagValue, String strFileDate) throws ParseException {
        DateTimeModifierFunction cut = new DateTimeModifierFunction(TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD.parse(strFileDate));
        return cut.apply(xmlTagName, xmlTagValue);
    }

    private void doTestWithDateFormat(String xmlTagValue, String strFileDate) throws ParseException {
        DateTimeModifierFunction cut = new DateTimeModifierFunction(TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD.parse(strFileDate));

        String newXmlTagValue = cut.apply("", xmlTagValue);
        TimelineLogger.info(getClass(), "Datei-Datum: " + strFileDate + ", Alter-Feld-Wert: " + xmlTagValue + ", Neuer-Feld-Wert: " + newXmlTagValue);

        Calendar fileCal = makeCalendarFrom(strFileDate);
        Calendar parsedOldFieldCal = makeCalendarFrom(xmlTagValue);
        Calendar parsedNewFieldCal = makeCalendarFrom(newXmlTagValue);
        int diffOfDays1 = cut.getDiffOfDays(fileCal.getTime(), new Date());
        int diffOfDays2 = cut.getDiffOfDays(parsedOldFieldCal.getTime(), parsedNewFieldCal.getTime());
        Assert.assertTrue(Math.abs(diffOfDays1 - diffOfDays2) < 2);
    }

    private Calendar makeCalendarFrom(String dateString) throws ParseException {
        Date parsedDate;
        try {
            parsedDate = TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse(dateString);
        } catch (ParseException e) {
            parsedDate = TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD.parse(dateString);
        }
        if (parsedDate == null) {
            Assert.fail("Datum " + dateString + " kann nicht geparst werden!");
        }
        Calendar parsedCal = Calendar.getInstance();
        parsedCal.setTime(parsedDate);
        return parsedCal;
    }

}
