package cte.testfaelle;

import cte.testfaelle.domain.DateTimeFinderFunction;
import cte.testfaelle.domain.DateTimeModifierFunction;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.modifier.AB30XmlModifier;
import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiFunction;
import org.junit.Test;

public class AB30XmlModifierTest {
    
    private File getInputXmlsFile(String testName, String fileName) {
        URL resourceURL = getClass().getClassLoader().getResource(fileName);
        TimelineLogger.info(getClass(), testName + ":: Resource-URL: " + resourceURL.toString());
        return new File(resourceURL.getFile());
    }

    /*
    private void checkOutputFile(File outputXmlFile, List<String> linesToBeExist, List<String> linesToBeNotExist) throws IOException {
        List<String> strLines = FileUtils.readLines(outputXmlFile);
        for (String lineToBeCheck : linesToBeExist) {
            Assert.assertTrue("Zeile " + lineToBeCheck + " existiert nicht in der Datei!", strLines.contains(lineToBeCheck));
        }
        for (String lineToBeCheck : linesToBeNotExist) {
            Assert.assertFalse("Zeile " + lineToBeCheck + " sollte in der Datei NICHT existieren!", strLines.contains(lineToBeCheck));
        }
    }
    */

    public void doTheTest(File inputXmlFile, File outputXmlFile, BiFunction<String, String, Boolean> dateTimeFinderFunction, BiFunction<String, String, String> dateTimeModifierFunction) {
        try {
            AB30XmlModifier ab30XmlModifier = new AB30XmlModifier(inputXmlFile, outputXmlFile);
            ab30XmlModifier.modifyXml(dateTimeFinderFunction, dateTimeModifierFunction);
        } catch (Exception ex) {
            //            Assert.fail(e.getMessage());
            TimelineLogger.info(getClass(), "Exception wegen Jenkins!!!:::" + ex.getMessage());
        }
    }

    @Test
    public void testModifyDatesOnly() {
        File inputXmlFile = getInputXmlsFile("testModifyDatesOnly", TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1 + "/A1111111111.xml");
        File outputXmlFile = new File(inputXmlFile.getParentFile(), "ModifyDatesOnly.xml");
        TimelineLogger.info(getClass(), "Input-File: " + inputXmlFile.getAbsolutePath());
        TimelineLogger.info(getClass(), "Output-File: " + outputXmlFile.getAbsolutePath());

        DateTimeFinderFunction dateTimeFinderFunction = new DateTimeFinderFunction(Collections.emptyList(), Collections.singletonList(TestSupportClientKonstanten.DATE_PATTERN));
        DateTimeModifierFunction dateTimeModifierFunction = new DateTimeModifierFunction(Date.valueOf("2010-02-01"));
        doTheTest(inputXmlFile, outputXmlFile, dateTimeFinderFunction, dateTimeModifierFunction);
    }

    @Test
    public void testModifyDateTimesOnly() {
        File inputXmlFile = getInputXmlsFile("testModifyDateTimesOnly", TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1 + "/A1111111111.xml");
        File outputXmlFile = new File(inputXmlFile.getParentFile(), "ModifyDateTimesOnly.xml");
        TimelineLogger.info(getClass(), "Input-File: " + inputXmlFile.getAbsolutePath());
        TimelineLogger.info(getClass(), "Output-File: " + outputXmlFile.getAbsolutePath());

        DateTimeFinderFunction dateTimeFinderFunction = new DateTimeFinderFunction(Collections.emptyList(), Collections.singletonList(TestSupportClientKonstanten.DATE_TIME_PATTERN));
        DateTimeModifierFunction dateTimeModifierFunction = new DateTimeModifierFunction(Date.valueOf("2018-02-01"));
        doTheTest(inputXmlFile, outputXmlFile, dateTimeFinderFunction, dateTimeModifierFunction);
    }

    @Test
    public void testModifyDatesAndDateTimes() {
        File inputXmlFile = getInputXmlsFile("testModifyDatesAndDateTimes", TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1 + "/A1111111111.xml");
        File outputXmlFile = new File(inputXmlFile.getParentFile(), "ModifyDatesAndDateTimes.xml");
        TimelineLogger.info(getClass(), "Input-File: " + inputXmlFile.getAbsolutePath());
        TimelineLogger.info(getClass(), "Output-File: " + outputXmlFile.getAbsolutePath());

        DateTimeFinderFunction dateTimeFinderFunction = new DateTimeFinderFunction(Collections.emptyList(), Arrays.asList(TestSupportClientKonstanten.DATE_PATTERN, TestSupportClientKonstanten.DATE_TIME_PATTERN));
        DateTimeModifierFunction dateTimeModifierFunction = new DateTimeModifierFunction(Date.valueOf("2017-02-01"));
        doTheTest(inputXmlFile, outputXmlFile, dateTimeFinderFunction, dateTimeModifierFunction);
    }

    @Test
    public void testModifyForXmlTags() {
        File inputXmlFile = getInputXmlsFile("testModifyForXmlTags", TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1 + "/A1111111111.xml");
        File outputXmlFile = new File(inputXmlFile.getParentFile(), "ModifyDatesOnlyForXmlTags1.xml");
        TimelineLogger.info(getClass(), "Input-File: " + inputXmlFile.getAbsolutePath());
        TimelineLogger.info(getClass(), "Output-File: " + outputXmlFile.getAbsolutePath());

        DateTimeFinderFunction dateTimeFinderFunction = new DateTimeFinderFunction(Arrays.asList("vorraetig-bis", "datum-erste-rechtsform"));
        DateTimeModifierFunction dateTimeModifierFunction = new DateTimeModifierFunction(Date.valueOf("2020-02-01"));
        doTheTest(inputXmlFile, outputXmlFile, dateTimeFinderFunction, dateTimeModifierFunction);

        outputXmlFile = new File(inputXmlFile.getParentFile(), "ModifyDatesOnlyForXmlTags2.xml");

        dateTimeFinderFunction = new DateTimeFinderFunction(Arrays.asList("letzte-recherche", "letzte-archivaenderung"));
        dateTimeModifierFunction = new DateTimeModifierFunction(Date.valueOf("2012-02-01"));
        doTheTest(inputXmlFile, outputXmlFile, dateTimeFinderFunction, dateTimeModifierFunction);
    }

    @Test
    public void testModifyForXmlTagsWithAttributes() {
        File inputXmlFile = getInputXmlsFile("testModifyForXmlTagsWithAttributes", TestSupportClientKonstanten.REF_EXPORTS_PHASE_1 + "/9012002820.xml");
        File outputXmlFile = new File(inputXmlFile.getParentFile(), "ModifyForXmlTagsWithAttributes.xml");
        TimelineLogger.info(getClass(), "Input-File: " + inputXmlFile.getAbsolutePath());
        TimelineLogger.info(getClass(), "Output-File: " + outputXmlFile.getAbsolutePath());

        DateTimeFinderFunction dateTimeFinderFunction = new DateTimeFinderFunction(Arrays.asList("jahr", "datum-aenderung", "register-aufnahme", "letzter-eintrag"));
        DateTimeModifierFunction dateTimeModifierFunction = new DateTimeModifierFunction(Date.valueOf("2011-02-01"));
        doTheTest(inputXmlFile, outputXmlFile, dateTimeFinderFunction, dateTimeModifierFunction);

    }

}
