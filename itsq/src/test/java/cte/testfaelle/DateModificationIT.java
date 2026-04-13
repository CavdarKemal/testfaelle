package cte.testfaelle;

import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import de.creditreform.crefoteam.cte.archivbestand30.util.AB30JaxbUtil;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Archivbestand;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.ObjectFactory;
import de.creditreform.crefoteam.cte.jaxbbasics.jaxbbasicscommon.JaxbBasicsRuntimeException;
import de.creditreform.crefoteam.cte.jaxbbasics.jaxbutil.CteJaxbBasics;
import de.creditreform.crefoteam.cte.jaxbbasics.jaxbutil.xml.JaxbBasicsMarshallerXML;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import de.creditreform.crefoteam.cte.testutils_cte.treevisitor.TreeFactoryReflection;
import de.creditreform.crefoteam.cte.testutils_cte.treevisitor.schnittstellen.TreeNode;
import de.creditreform.crefoteam.cte.testutils_cte.treevisitor.schnittstellen.TreeVisitor;
import de.creditreform.crefoteam.cte.testutils_cte.treevisitor.schnittstellen.TreeVisitorMode;
import de.creditreform.crefoteam.cte.testutils_cte.treevisitor.visitors.CountTypeNamesVisitor;
import de.creditreform.crefoteam.cte.testutils_cte.treevisitor.visitors.UpdateDateVisitor;
import de.creditreform.crefoteam.cte.testutils_cte.treevisitor.visitors.updateoperator.DateUpdateOperator;
import de.creditreform.crefoteam.cte.testutils_cte.treevisitor.visitors.updateoperator.DateUpdateOperatorFixed;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integrations-Test für die Manipulation von Datums-Angaben mit Hilfe des Moduls testutils_cte:treevisitor
 * Wie in CTEWE-1902 beschrieben, ist der Erhalt von Kommentaren in den Testdaten unverzichtbar. Da dies mit
 * JAXB nicht realistisch ist, erfordern alle damit durchgeführten Manipulationen eine manuelle Nachbearbeitung.
 * Wenn man berücksichtigt, dass die hier verwendeten Klassen auf eine Massen-Verarbeitung ausgerichtet sind,
 * muss dieser Test als interessanter aber erfolgloser Probelauf gelten.
 */
public class DateModificationIT {

    protected static final AB30JaxbUtil AB30_JAXB_UTIL = new AB30JaxbUtil();
    protected static final CteJaxbBasics JAXB_BASICS = new CteJaxbBasics(Archivbestand.class.getPackage());
    @Rule
    public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();
    private File phase2SourceDir;
    private File targetDir;
    private JaxbBasicsMarshallerXML marshallerXML;
    private ObjectFactory ab30Obf;

    @Before
    public void setUp() {
        File srcArchivBestandDir = new File(ITSQTestFaelleUtilTest.TEST_SET_DIR, TestSupportClientKonstanten.ARCHIV_BESTAND_ROOT);
        phase2SourceDir = new File(srcArchivBestandDir, TestSupportClientKonstanten.TEST_PHASE.PHASE_2.getDirName());
        targetDir = new File("./target/DateModificationIT");
        marshallerXML = JAXB_BASICS.marshalXML(null, null);
        ab30Obf = new ObjectFactory();
    }

    @Test
    public void testCountTypeNames() throws IOException {
        final TreeNode<Archivbestand, Archivbestand> treeAB30 = TreeFactoryReflection.buildAB30Tree();
        CountTypeNamesVisitor visitor = new CountTypeNamesVisitor();
        Assert.assertTrue(phase2SourceDir.isDirectory());
        // Liste der Dateien ermitteln...
        int fileCounter = 0;
        List<File> ph2XmlsList = Arrays.stream(phase2SourceDir.listFiles((dir, name) -> name.endsWith(".xml"))).collect(Collectors.toList());
        for (File f : ph2XmlsList) {
            TimelineLogger.info(getClass(), "Counting type-names in {}", f.getName());
            readAndVisitFile(treeAB30, visitor, f);
            fileCounter++;
        }
        List<Map.Entry<String, Long>> sortedResults = visitor.getSortedResults();
        Assert.assertFalse(sortedResults.isEmpty());
        Logger logger = LoggerFactory.getLogger(getClass());
        TimelineLogger.info(getClass(), "=== Anzahl der verarbeiteten Dateien: {}", fileCounter);
        for (Map.Entry<String, Long> e : sortedResults) {
            TimelineLogger.info(getClass(), "{} -> {}", e.getValue(), e.getKey());
        }
    }

    @Test
    public void testModifyDates() throws IOException {
        // Tree und TreeVisitor vorbereiten...
        // - Baum vollständig (per Reflection) anlegen...
        final TreeNode<Archivbestand, Archivbestand> treeAB30 = TreeFactoryReflection.buildAB30Tree();
        Assert.assertNotNull(treeAB30);
        // - Art der Manipulation (plus 1 Jahr, 2 Monate, 3 Tage)...
        DateUpdateOperator updateOperator = new DateUpdateOperatorFixed(1, 2, 3);
        // - Visitor für die Durchführung des Updates...
        UpdateDateVisitor updateVisitor = new UpdateDateVisitor(updateOperator);

        // Verzeichnisse checken
        Assert.assertTrue(phase2SourceDir.exists() && phase2SourceDir.isDirectory());
        targetDir.mkdirs();
        Assert.assertTrue(targetDir.isDirectory());

        // Liste der Dateien ermitteln...
        // Laufzeiten:
        // 1. Das Logging des Dateinamens 'kostet' etwa 2 Sekunden
        // 2. Logging, Einlesen und Wegschreiben dauern zusammen etwa 2,5 Sekunden (2663 Dateien in Phase 2)
        // 3. Inklusive Datums-Modifikation dauert ein Durchlauf für alle Crefos etwa 3,0 Sekunden (P53 Laptop)
        //    Als Differenz zu Punkt 2 bleiben etwa 0,5 Sekunden für das Update aller Datums-Angaben in ~2663 Crefos
        Assert.assertTrue(phase2SourceDir.isDirectory());
        int fileCounter = 0;
        final long timestampStart = System.currentTimeMillis();
        List<File> ph2XmlsList = Arrays.stream(phase2SourceDir.listFiles((dir, name) -> name.endsWith(".xml"))).collect(Collectors.toList());
        for (File f : ph2XmlsList) {
            TimelineLogger.info(getClass(), "Updating {}", f.getName());
            readAndModifyFile(treeAB30, updateVisitor, f);
            fileCounter++;
        }
        final long timestampEnd = System.currentTimeMillis();
        TimelineLogger.info(getClass(), "=== Anzahl der verarbeiteten Dateien: {}", fileCounter);
        TimelineLogger.info(getClass(), "=== Laufzeit: {} ms", timestampEnd - timestampStart);
    }

    private void readAndModifyFile(TreeNode<Archivbestand, Archivbestand> treeAB30, UpdateDateVisitor updateVisitor, File f) throws IOException {
        File targetFile = new File(targetDir, f.getName());
        try (FileInputStream fis = new FileInputStream(f);
             FileOutputStream fos = new FileOutputStream(targetFile, false)) {
            // Einlesen...
            Archivbestand ab30 = null;
            ab30 = AB30_JAXB_UTIL.unmarshal(fis);
            // Modifizieren...
            treeAB30.accept(updateVisitor, TreeVisitorMode.VISIT_EXISTING_CONTENT.create(), ab30);
            // Wegschreiben...
            marshallerXML.toStream(fos, ab30Obf.createArchivbestand(ab30));
        } catch (JaxbBasicsRuntimeException ex) {
            throw new RuntimeException("Fehler beim Unmarshalln der Datei " + targetFile.getAbsolutePath(), ex);
        }

    }

    private void readAndVisitFile(TreeNode<Archivbestand, Archivbestand> treeAB30, TreeVisitor visitor, File f) throws IOException {
        try (FileInputStream fis = new FileInputStream(f)) {
            // Einlesen...
            Archivbestand ab30 = AB30_JAXB_UTIL.unmarshal(fis);
            // Visitor anwenden...
            treeAB30.accept(visitor, TreeVisitorMode.VISIT_EXISTING_CONTENT.create(), ab30);
        } catch (JaxbBasicsRuntimeException ex) {
            throw new RuntimeException("Fehler beim Unmarshalln der Datei " + f.getAbsolutePath(), ex);
        }
    }

}
