package cte.testfaelle.extender;

import cte.testfaelle.domain.AB30XMLProperties;
import cte.testfaelle.domain.TestCustomer;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang.time.DateFormatUtils;

public class ExtendArchivBestandCrefos {

    final File testSetRootDir;
    final File ab30RootDir;
    private final Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomersMapMap;
    private final AB30MapperUtil ab30MapperUtil;

    public ExtendArchivBestandCrefos(Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomersMapMap, File testSetRootDir) {
        this.activeCustomersMapMap = activeCustomersMapMap;
        this.testSetRootDir = testSetRootDir;
        this.ab30RootDir = new File(testSetRootDir, TestSupportClientKonstanten.ARCHIV_BESTAND_ROOT);
        this.ab30MapperUtil = new AB30MapperUtil();
    }

    private File backupAndCreateNewFile(TestSupportClientKonstanten.TEST_PHASE testPhase, File ab30PhaseXmlsDir, String fileName) {
        File newFile = new File(ab30PhaseXmlsDir, fileName);
        if (newFile.exists()) {
            TimelineLogger.info(getClass(), "ExtendArchivBestandCrefos#doForPhase(" + testPhase.getDirName() + ") :: Bennene '" + fileName + "' um...");
            File oldFile = new File(ab30PhaseXmlsDir, fileName + DateFormatUtils.format(Calendar.getInstance(), ".yyyy-MM-dd HH-mm-ss"));
            oldFile.delete();
            boolean isOK = newFile.renameTo(oldFile);
            if (!isOK) {
                throw new RuntimeException("ExtendArchivBestandCrefos#doForPhase(" + testPhase.getDirName() + ") :: Konnte '" + fileName + "' nicht umbenennen!");
            }
        }
        return newFile;
    }

    public void extendTestCrefos() throws Exception {
        Iterator<TestSupportClientKonstanten.TEST_PHASE> phaseIterator = activeCustomersMapMap.keySet().iterator();
        while (phaseIterator.hasNext()) {
            TestSupportClientKonstanten.TEST_PHASE testPhase = phaseIterator.next();
            doForPhase(testPhase);
        }
    }

    public void doForPhase(TestSupportClientKonstanten.TEST_PHASE testPhase) throws Exception {
        File ab30PhaseXmlsDir = new File(ab30RootDir, testPhase.getDirName());

        TimelineLogger.info(getClass(), "ExtendArchivBestandCrefos#doForPhase(" + testPhase.getDirName() + ") :: Initialisiere eine neue AB30XMLProperties-Map aus den Testfällen für in customerTestInfoMap befindlichen Testfällen...");
        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap = ab30MapperUtil.initAb30CrefoPropertiesMapFromRefExports("", ab30PhaseXmlsDir, activeCustomersMapMap, testPhase);

        TimelineLogger.info(getClass(), "ExtendArchivBestandCrefos#doForPhase(" + testPhase.getDirName() + ") :: erweitere die Map um AB30XMLProperties-Einträge für Beteiligten bzw. Entschedidungsträger der TestCrefo, falls nicht vorhanden...");
        ab30CrefoToPropertiesMap = ab30MapperUtil.extendAb30CrefoPropertiesMapWithBtlgs("", ab30PhaseXmlsDir, ab30CrefoToPropertiesMap);

        File testCrefosFile = new File(ab30PhaseXmlsDir, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        TimelineLogger.info(getClass(), "ExtendArchivBestandCrefos#doForPhase(" + testPhase.getDirName() + ") :: Ergänze Attributes von AB30XMLProperties-Map  aus altem 'TestCrefos.properties' - Datei...");
        ab30CrefoToPropertiesMap = ab30MapperUtil.extendAb30CrefoPropertiesWithOldAttributes("", testCrefosFile, ab30CrefoToPropertiesMap);

        File newPorpsFile = backupAndCreateNewFile(testPhase, ab30PhaseXmlsDir, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        TimelineLogger.info(getClass(), "ExtendArchivBestandCrefos#doForPhase(" + testPhase.getDirName() + ") :: Erzeuge Datei '" + newPorpsFile.getName() + "'...");
        ab30MapperUtil.writeAb30CrefoToPropertiesMapToFile(newPorpsFile, ab30CrefoToPropertiesMap);

        newPorpsFile = backupAndCreateNewFile(testPhase, ab30PhaseXmlsDir, TestSupportClientKonstanten.CREFOS_TO_CUSTOMERS_MAP_FILENAME);
        TimelineLogger.info(getClass(), "ExtendArchivBestandCrefos#doForPhase(" + testPhase.getDirName() + ") :: Erzeuge Datei " + TestSupportClientKonstanten.CREFOS_TO_CUSTOMERS_MAP_FILENAME + ", in der die Crefos gruppiert nach Kunde aufgelistet werden");
        ab30MapperUtil.writeCrefoToCustomerMappingFile(newPorpsFile, ab30CrefoToPropertiesMap);
    }

}