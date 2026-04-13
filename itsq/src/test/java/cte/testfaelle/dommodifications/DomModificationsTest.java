package cte.testfaelle.dommodifications;

import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModException;
import cte.testfaelle.dommodifications.dommodcommon.DomModExceptionUnknownInstruction;
import cte.testfaelle.dommodifications.dommodlisteners.DomModNodeListenerInstructionProcessor;
import cte.testfaelle.dommodifications.dommodlisteners.DomModNodeListenerInstructionValidator;
import cte.testfaelle.dommodifications.dommodmarshalling.DomModMarshallingStrategy;
import cte.testfaelle.dommodifications.dommodmarshalling.DomModMarshallingStrategyJAXP;
import cte.testfaelle.dommodifications.dommodmarshalling.DomModMarshallingStrategyLS;
import de.creditreform.crefoteam.cte.archivbestand30.util.AB30JaxbUtil;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Archivbestand;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

public class DomModificationsTest {
    protected static final AB30JaxbUtil AB30_JAXB_UTIL = new AB30JaxbUtil().createValidatingInstance();
    protected static final File srcToValidate = new File("src/test/resources/dom-modifications/4100209464.xml");
    protected static final File srcDefekt = new File("src/test/resources/dom-modifications/4100209464.xml-defekt");

    private static void verifyUsingXSD(Path p)
            throws IOException {
        Assert.assertTrue(Files.exists(p));
        Assert.assertTrue(Files.isRegularFile(p));
        try (InputStream fis = Files.newInputStream(p)) {
            Archivbestand ab30Validated = AB30_JAXB_UTIL.unmarshal(fis);
            Assert.assertNotNull(ab30Validated);
        }
    }

    private static void checkRejectXXE(DomModMarshallingStrategy marshallingStrategy)
            throws IOException {
        // Erst mal der Positiv-Test...
        File nonXxeXML = new File("./src/test/resources/dom-modifications-xxetest/test_sauber.xml");
        Assert.assertTrue("XML-Datei ohne XXE-Include fehlt", nonXxeXML.exists() && nonXxeXML.isFile());
        try (InputStream is = Files.newInputStream(nonXxeXML.toPath())) {
            Document nonXxeDocument = marshallingStrategy.parseFromStream(is);
            Assert.assertNotNull(nonXxeDocument);
        } catch (DomModException e) {
            throw new AssertionError("Parsen des sauberen XML-Dokumentes ohne XXE gescheitert", e);
        }

        // Jetzt der Test, ob XXE abgewiesen wird...
        File xxeXML = new File("./src/test/resources/dom-modifications-xxetest/test_xxe.xml");
        Assert.assertTrue("XML-Datei mit dem XXE-Include fehlt", xxeXML.exists() && xxeXML.isFile());
        File xxeInclude = new File("./test_xxe.txt");
        Assert.assertTrue("per XXE inkludierte Textdatei fehlt", xxeInclude.exists() && xxeInclude.isFile());

        try (InputStream is = Files.newInputStream(xxeXML.toPath())) {
            Document xxeDocument = marshallingStrategy.parseFromStream(is);
            Assert.fail("XML-Datei mit XXE-Include nicht abgewiesen durch " + marshallingStrategy.getClass().getSimpleName());
        } catch (DomModException e) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Wir wollen sicher sein, dass die Eingangsdaten inklusive Processing-Instruction weiterhin valide im Sinne
     * des XSD sind
     */
    @Test
    public void verifyInputXml()
            throws IOException {
        verifyUsingXSD(srcToValidate.toPath());
    }

    protected int countProcessingInstructions(Path p) {
        DomModNodeListenerInstructionValidator validator = new DomModNodeListenerInstructionValidator();
        DomModTreeWalker treeWalker = new DomModTreeWalker(validator);
        DomModContext ctx = new DomModContext(new Date());
        treeWalker.walkDOMTree(ctx, new DomModMarshallingStrategyLS(), p);
        return validator.getInstructionsFound();
    }

    /**
     * Im Anschluss an die Verarbeitung der Processing-Instructions muss eine Validierung gegen das XSD weiterhin
     * erfolgreich sein
     */
    @Test
    public void verifyOutputXml()
            throws IOException {
        Path targetDir = new File("./target").toPath();
        Path srcPath = srcToValidate.toPath();
        // zähle die enthaltenen Processing-Instructions...
        int expected = countProcessingInstructions(srcPath);
        Assert.assertTrue("Dieser Test ist nur dann sinnvoll, wenn die Quelle Processing-Instructions beinhaltet", expected > 0);
        // erzeuge die modifizierte Datei...
        DomModCopyTool copyTool = new DomModCopyTool(new DomModMarshallingStrategyLS(), targetDir);
        DomModContext ctx = new DomModContext(new Date());
        Path outFile = copyTool.copySingle(ctx, new DomModNodeListenerInstructionProcessor(), srcPath);
        verifyUsingXSD(outFile);
        // prüfe die Processing-Instructions in der Ausgabe
        int actual = countProcessingInstructions(outFile);
        Assert.assertEquals("Durch das Verarbeiten der Processing-Instructions sollte deren Anzahl nicht verändert werden", expected, actual);
    }

    @Test
    public void testDetectIllegalInstruction() {
        DomModNodeListenerInstructionValidator validator = new DomModNodeListenerInstructionValidator();
        Path srcPath = srcDefekt.toPath();
        DomModTreeWalker treeWalker = new DomModTreeWalker(validator);
        DomModContext ctx = new DomModContext(new Date());
        try {
            treeWalker.walkDOMTree(ctx, new DomModMarshallingStrategyLS(), srcPath);
            Assert.fail("Illegale Processing-Instruction nicht abgewiesen");
        } catch (DomModExceptionUnknownInstruction e) {
            Assert.assertTrue("Die im defekten XML vorliegende Processing-Instruction wird in der Fehlermeldung nicht genannt",
                    e.getMessage().contains("ILLEGALE_INSTRUKTION"));
        }

    }

    /**
     * Bei Parsing eines XML-Dokumentes, welches ein XXE-Includde verwendet, sollte eine Exception (genauer gesagt
     * LSException) fliegen. Die {@link DomModMarshallingStrategy} sollte diese in eine DomModException verpacken.
     */
    @Test
    public void testXXE()
            throws IOException {
        checkRejectXXE(new DomModMarshallingStrategyLS());
        checkRejectXXE(new DomModMarshallingStrategyJAXP());
    }

}
