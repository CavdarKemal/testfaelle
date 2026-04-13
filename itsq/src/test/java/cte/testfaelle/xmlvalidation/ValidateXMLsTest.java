package cte.testfaelle.xmlvalidation;

import cte.testfaelle.ITSQTestFaelleUtilTest;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import de.creditreform.crefoteam.cte.archivbestand30.util.AB30Validator;
import de.creditreform.crefoteam.cte.archivbestand30.util.AB30ValidatorException;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Archivbestand;
import de.creditreform.crefoteam.cte.jaxbbasics.xmlvalidation.XmlValidation;
import de.creditreform.crefoteam.cte.jaxbbasics.xmlvalidation.XmlValidationImplDOM;
import de.creditreform.crefoteam.cte.jaxbbasics.xmlvalidation.XmlValidationMode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class ValidateXMLsTest {
    protected static final AB30Validator AB30_VALIDATOR = new AB30Validator();
    protected static final Pattern CREFONUMMER_PATTERN = Pattern.compile("\\d{10}\\.xml");

    @Test
    public void testValidateUsingAB30_VALIDATOR() throws IOException {
        TimelineLogger.info(getClass(), "===================================================================================================");
        List<File> invalideXMLsList;
        invalideXMLsList = validateXmlFiles(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1));
        if (!invalideXMLsList.isEmpty()) {
            String errMsg = "Invalide XMLs in " + TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1 + ": " + invalideXMLsList;
            TimelineLogger.error(getClass(), errMsg);
            Assert.assertTrue(errMsg, invalideXMLsList.isEmpty());
        }
        invalideXMLsList = validateXmlFiles(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2));
        if (!invalideXMLsList.isEmpty()) {
            String errMsg = "Invalide XMLs in " + TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2 + ": " + invalideXMLsList;
            TimelineLogger.error(getClass(),  errMsg);
            Assert.assertTrue(errMsg, invalideXMLsList.isEmpty());
        }
    }

    private List<File> validateXmlFiles(File fileParentDir) throws IOException {
        List<File> invalideXMLsList = new ArrayList<>();
        TimelineLogger.info(getClass(), "Validiere AB30-Dateien " + fileParentDir + " ...");
        for (File ab30File : fileParentDir.listFiles()) {
            String name = ab30File.getName();
            Matcher matcher = CREFONUMMER_PATTERN.matcher(name);
            if (matcher.find()) {
                try {
                    final Archivbestand archivbestand = AB30_VALIDATOR.validateAndParse(FileUtils.readFileToString(ab30File));
                } catch (AB30ValidatorException ex) {
                    TimelineLogger.error(getClass(),  "XML-Validierung für '" + ab30File.getAbsolutePath() + "' fehlgeschlagen: ");
                    invalideXMLsList.add(ab30File);
                }
            }
        }
        return invalideXMLsList;
    }

    @Test
    public void testValidateSchemaUsingXmlValidationImplDOM() throws IOException {
        XmlValidation cutAB30 = new XmlValidationImplDOM().withNamespaceUrl("http://www.creditreform.de/crefoteam/archivbestandv3_0").withSchemaInstance("/META-INF/schnittstellen/archivbestand30/archivbestandv3_0.xsd");
        List<File> invalideXMLsList = validate(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1), cutAB30);
        if (!invalideXMLsList.isEmpty()) {
            String errMsg = "Invalide XMLs in " + TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1 + ": " + invalideXMLsList;
            TimelineLogger.error(getClass(),  errMsg);
            Assert.assertTrue(errMsg, invalideXMLsList.isEmpty());
        }

        invalideXMLsList = validate(new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2), cutAB30);
        if (!invalideXMLsList.isEmpty()) {
            String errMsg = "Invalide XMLs in " + TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2 + ": " + invalideXMLsList;
            TimelineLogger.error(getClass(),  errMsg);
            Assert.assertTrue(errMsg, invalideXMLsList.isEmpty());
        }
    }

    private List<File> validate(File fileParentDir, XmlValidation cutAB30) throws IOException {
        List<File> invalideXMLsList = new ArrayList<>();
        TimelineLogger.info(getClass(), "===================================================================================================");
        TimelineLogger.info(getClass(), "Validiere AB30-Dateien " + fileParentDir + " ...");
        for (File ab30File : fileParentDir.listFiles()) {
            String name = ab30File.getName();
            Matcher matcher = CREFONUMMER_PATTERN.matcher(name);
            if (matcher.find()) {
                //TestUtil.TimelineLogger.info(getClass(), "\tValidiere AB30-Datei " + TestUtil.getShortPath(ab30File, pathToParentDir) + " ...");
                XmlValidation.XmlValidationError xmlValidationError = cutAB30.checkXml(XmlValidationMode.VALIDIERE_XSD_SCHEMA, FileUtils.readFileToString(ab30File));
                if (xmlValidationError != null) {
                    TimelineLogger.error(getClass(),  "XML-Validierung für '" + ab30File.getAbsolutePath() + "' fehlgeschlagen: " + xmlValidationError.getMessage());
                    invalideXMLsList.add(ab30File);
                }
            }
        }
        TimelineLogger.info(getClass(), "---------------------------------------------------------------------------------------------------");
        return invalideXMLsList;
    }
}
