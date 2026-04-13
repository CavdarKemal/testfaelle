package cte.testfaelle.consistency;

import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Assert;

public class RohdatenConsistencyBase {

    protected Map<Long, RohdatenBeschreibung> scanParentDir(RohdatenParserIF parser, File fileParentDir) {
        Map<Long, RohdatenBeschreibung> targetMap = new TreeMap<>();
        Assert.assertNotNull(fileParentDir);
        TimelineLogger.info(getClass(), "Parse AB30-Dateien " + fileParentDir.getName() + " ...");
        Assert.assertNotNull(fileParentDir);
        if (!fileParentDir.exists()) {
            File currentDir = new File(".");
            Assert.fail("!!! Basis-Verzeichnis existiert nicht: " + fileParentDir.getName() + "\naktueller Pfad ist: " + currentDir.getAbsolutePath());
        }
        for (File ab30File : fileParentDir.listFiles()) {
            if (ab30File.getName().endsWith(".xml")) {
                TimelineLogger.info(getClass(), "\tParse AB30-Datei " + ITSQTestFaelleUtil.getShortPath(ab30File, fileParentDir.getPath()) + " ...");
                RohdatenBeschreibung rohdatenBeschreibung = parser.parseFile(ab30File);
                if (rohdatenBeschreibung != null) {
                    TimelineLogger.info(getClass(), "\t\t==> " + rohdatenBeschreibung);
                    targetMap.put(rohdatenBeschreibung.getCrefonummer(), rohdatenBeschreibung);
                }
            }
        }
        return targetMap;
    }
}
