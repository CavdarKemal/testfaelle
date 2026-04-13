package cte.testfaelle.dommodifications;

import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodlisteners.DomModNodeListener;
import cte.testfaelle.dommodifications.dommodlisteners.DomModNodeListenerChain;
import cte.testfaelle.dommodifications.dommodlisteners.DomModNodeListenerInstructionProcessor;
import cte.testfaelle.dommodifications.dommodlisteners.DomModNodeListenerLog;
import cte.testfaelle.dommodifications.dommodmarshalling.DomModMarshallingStrategy;
import cte.testfaelle.dommodifications.dommodmarshalling.DomModMarshallingStrategyJAXP;
import cte.testfaelle.dommodifications.dommodmarshalling.DomModMarshallingStrategyLS;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import java.io.File;
import java.util.Date;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Utility-Klasse für die Modifikation aller XML-Dateien in einem Verzeichnis mittels DOM
 */
public class DomModificationsJob {
    protected static final File PARENT_SRC = new File("./src/test/resources");
    protected static final File PARENT_TARGET = new File("./target");
    protected static final String SUBDIR_NAME = "dom-modifications";
    @Rule
    public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();

    @Test
    public void testDomModifications() {
        pruefeModifikationViaDOM(new DomModMarshallingStrategyJAXP());
        pruefeModifikationViaDOM(new DomModMarshallingStrategyLS());
    }

    protected void pruefeModifikationViaDOM(DomModMarshallingStrategy marshallingStrategy) {
        Assert.assertTrue(PARENT_SRC.exists());
        File srcSubdir = new File(PARENT_SRC, SUBDIR_NAME);

        Assert.assertTrue(PARENT_TARGET.exists());
        File targetSubdir = new File(PARENT_TARGET, SUBDIR_NAME);
        targetSubdir.mkdirs();
        // Wir wollen sowohl ein Logging als auch das Abarbeiten der Processing-Instructions...
        // Da DomModNodeListenerLog zuerst genannt wird, protokollieren wir den unveränderten Stand. Mit
        // umgekehrter Reihenfolge würde das Ergebnis der Änderung ausgegeben.
        DomModNodeListener nodeListener = new DomModNodeListenerChain(new DomModNodeListenerLog(), new DomModNodeListenerInstructionProcessor());
        DomModCopyTool copyTool = new DomModCopyTool(marshallingStrategy, targetSubdir.toPath());
        DomModContext ctx = new DomModContext(new Date());
        int anzFiles = copyTool.copyDirectoryContent(ctx, nodeListener, srcSubdir.toPath(), (p) -> p.getFileName().toString().endsWith(".xml"));
        TimelineLogger.info(getClass(), "Copied " + anzFiles + " files into " + targetSubdir.getAbsolutePath());

    }

}
