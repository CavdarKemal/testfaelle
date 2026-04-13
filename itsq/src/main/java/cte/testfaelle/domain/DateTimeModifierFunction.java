package cte.testfaelle.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class DateTimeModifierFunction implements BiFunction<String, String, String> {

    private final Date lasdtModifiedDate;

    public DateTimeModifierFunction(Date lasdtModifiedDate) {
        this.lasdtModifiedDate = lasdtModifiedDate;
    }

    public DateTimeModifierFunction(long lastModified) {
        lasdtModifiedDate = new Date(lastModified);
    }

    public Date getLasdtModifiedDate() {
        return lasdtModifiedDate;
    }

    @Override
    public String apply(String xmlTagName, String foundXmlTagValue) {
        if (foundXmlTagValue != null) {
            try {
                return calculateNewDateTimeValue(foundXmlTagValue);
            } catch (ParseException parseException) {
                return calculateNewAmount(xmlTagName, foundXmlTagValue);
            } catch (Exception ex) {
                TimelineLogger.error(getClass(), "\t\t!!!Fehler beim Ermitteln des neuen Wertes für das XML-Tag '" + xmlTagName + "'!\n" + ex.getMessage());
            }
        }
        return foundXmlTagValue;
    }

    public int getDiffOfDays(Date d1, Date d2) {
        long diff = d1.getTime() - d2.getTime();
        long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return Long.valueOf(days).intValue();
    }

    private String calculateNewAmount(String xmlTagName, String foundXmlTagValue) {
        // abh. vom xmlTagName den Wert ermitteln...
        int calField;
        if (xmlTagName.equalsIgnoreCase("Jahr")) {
            calField = Calendar.YEAR;
            int diffDays = getDiffOfDays(new Date(), lasdtModifiedDate);
            Calendar foundXmlTagCal = Calendar.getInstance();
            try {
                foundXmlTagCal.set(calField, Integer.valueOf(foundXmlTagValue));
                foundXmlTagCal.add(Calendar.DAY_OF_MONTH, diffDays);
                return String.valueOf(foundXmlTagCal.get(calField));
            } catch (NumberFormatException ex) {
                TimelineLogger.error(getClass(), "\t\t!!!Das XML-Tag '" + xmlTagName + "' mit dem Wert '" + foundXmlTagValue + "' wird nicht unterstützt!");
                return foundXmlTagValue;
            }
        } else {
            TimelineLogger.error(getClass(), "\t\t!!!Das XML-Tag '" + xmlTagName + "' mit dem Wert '" + foundXmlTagValue + "' wird nicht unterstützt!");
            return foundXmlTagValue;
        }
    }

    private String calculateNewDateTimeValue(String foundXmlTagValue) throws ParseException {
        Date fieldDate;
        SimpleDateFormat dateFormat;
        try {
            fieldDate = TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse(foundXmlTagValue);
            dateFormat = TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS;
        } catch (ParseException parseException) {
            fieldDate = TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD.parse(foundXmlTagValue);
            dateFormat = TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD;
        }
        int diffDays = getDiffOfDays(new Date(), lasdtModifiedDate);
        Calendar tmpCal = Calendar.getInstance();
        tmpCal.setTime(fieldDate);
        tmpCal.add(Calendar.DAY_OF_MONTH, diffDays);

        String strNewDate = dateFormat.format(tmpCal.getTime());
        String strOutputDate = strNewDate + foundXmlTagValue.substring(strNewDate.length());
        return strOutputDate;
    }
}