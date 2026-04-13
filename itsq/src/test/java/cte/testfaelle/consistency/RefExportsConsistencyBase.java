package cte.testfaelle.consistency;

import cte.testfaelle.consistency.parser.AtfRefExportsParser;
import cte.testfaelle.consistency.parser.BdrRefExportsParser;
import cte.testfaelle.consistency.parser.BvdRefExportsParser;
import cte.testfaelle.consistency.parser.CefRefExportsParser;
import cte.testfaelle.consistency.parser.CrmRefExportsParser;
import cte.testfaelle.consistency.parser.CtcRefExportsParser;
import cte.testfaelle.consistency.parser.DefaultRefExportsParser;
import cte.testfaelle.consistency.parser.DfoRefExportsParser;
import cte.testfaelle.consistency.parser.DnpRefExportsParser;
import cte.testfaelle.consistency.parser.DrdRefExportsParser;
import cte.testfaelle.consistency.parser.EhRefExportsParser;
import cte.testfaelle.consistency.parser.FooRefExportsParser;
import cte.testfaelle.consistency.parser.FsuRefExportsParser;
import cte.testfaelle.consistency.parser.FwRefExportsParser;
import cte.testfaelle.consistency.parser.GdlRefExportsParser;
import cte.testfaelle.consistency.parser.IkaRefExportsParser;
import cte.testfaelle.consistency.parser.InsoRefExportsParser;
import cte.testfaelle.consistency.parser.IsmRefExportsParser;
import cte.testfaelle.consistency.parser.LenRefExportsParser;
import cte.testfaelle.consistency.parser.MicRefExportsParser;
import cte.testfaelle.consistency.parser.NimRefExportsParser;
import cte.testfaelle.consistency.parser.PpaRefExportsParser;
import cte.testfaelle.consistency.parser.RefExportsParserIF;
import cte.testfaelle.consistency.parser.RtnRefExportsParser;
import cte.testfaelle.consistency.parser.SdfRefExportsParser;
import cte.testfaelle.consistency.parser.TrdiRefExportsParser;
import cte.testfaelle.consistency.parser.VsxRefExportsParser;
import cte.testfaelle.consistency.parser.ZewRefExportsParser;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.extender.ITSQTestFaelleUtil;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.Assert;

public class RefExportsConsistencyBase {

    public static Map<String, RefExportsParserIF> refExportsParserMap = new TreeMap<>() {{
        put("atf", new AtfRefExportsParser());
        put("bdr", new BdrRefExportsParser());
        put("bic", new DefaultRefExportsParser());
        put("bvd", new BvdRefExportsParser());
        put("cef", new CefRefExportsParser());
        put("crm", new CrmRefExportsParser());
        put("ctc", new CtcRefExportsParser());
        put("dfo", new DfoRefExportsParser());
        put("dnp", new DnpRefExportsParser());
        put("drd", new DrdRefExportsParser());
        put("eh", new EhRefExportsParser());
        put("fsu", new FsuRefExportsParser());
        put("foo", new FooRefExportsParser());
        put("fw", new FwRefExportsParser());
        put("gdl", new GdlRefExportsParser());
        put("ika", new IkaRefExportsParser());
        put("inso_kundenplz", new InsoRefExportsParser());
        put("inso_test-tool", new InsoRefExportsParser());
        put("ism", new IsmRefExportsParser());
        put("len", new LenRefExportsParser());
        put("mic", new MicRefExportsParser());
        put("mip", new DefaultRefExportsParser());
        put("nim", new NimRefExportsParser());
        put("nvi", new DefaultRefExportsParser());
        put("pni", new PpaRefExportsParser());
        put("ppa", new PpaRefExportsParser());
        put("rtn", new RtnRefExportsParser());
        put("sdf_daily", new SdfRefExportsParser());
        put("vsd", new VsxRefExportsParser());
        put("trdi", new TrdiRefExportsParser());
        put("vsh", new VsxRefExportsParser());
        put("vso", new VsxRefExportsParser());
        put("zew", new ZewRefExportsParser());
    }};

/*
    public Map<Long, RefExportsBeschreibung> scanParentDir(File fileParentDir) {
        Map<Long, RefExportsBeschreibung> targetMap = new TreeMap<>();
        TimelineLogger.info(getClass(), "Parse REF-EXPORTS-Dateien " + fileParentDir.getName() + " ...");
        Assert.assertNotNull(fileParentDir);
        if (!fileParentDir.exists()) {
            File currentDir = new File(".");
            Assert.fail("!!! Basis-Verzeichnis existiert nicht: " + fileParentDir.getName() + "\naktueller Pfad ist: " + ITSQTestFaelleUtil.getShortPath(currentDir, fileParentDir.getPath()) + " ...");
        }
        File[] custemerFiles = fileParentDir.listFiles();
        for (File custemerFile : custemerFiles) {
            TimelineLogger.info(getClass(), "\tParse REF-Export für Kunden " + ITSQTestFaelleUtil.getShortPath(custemerFile, TestSupportClientKonstanten.REF_EXPORTS_ROOT) + " ...");
            RefExportsParserIF refExportsParser = refExportsParserMap.get(custemerFile.getName().toLowerCase(Locale.ROOT));
            if (refExportsParser == null) {
                TimelineLogger.error(getClass(),  "\t\t\t\t!!!Die Map enthält keinen Parser für den Kunden!" + custemerFile.getName());
                continue;
            }
            List<File> scenarioFilesList = Arrays.stream(custemerFile.listFiles((dir, name) -> new File(dir, name).isDirectory())).collect(Collectors.toList());
            for (File scenarioFile : scenarioFilesList) {
                TimelineLogger.info(getClass(), "\t\tParse REF-Export für Schenario " + ITSQTestFaelleUtil.getShortPath(scenarioFile, TestSupportClientKonstanten.REF_EXPORTS_ROOT) + " ...");
                File[] refExpFiles = scenarioFile.listFiles();
                for (File refExpFile : refExpFiles) {
                    if (refExpFile.getName().endsWith(".xml") && !refExpFile.getName().startsWith("XML")) {
                        TimelineLogger.info(getClass(), "\t\t\tParse REF-Export-Datei " + ITSQTestFaelleUtil.getShortPath(refExpFile, TestSupportClientKonstanten.REF_EXPORTS_ROOT) + " ...");
                        RefExportsBeschreibung refExportsBeschreibung = refExportsParser.parseFile(refExpFile);
                        if (refExportsBeschreibung != null) {
                            targetMap.put(refExportsBeschreibung.getCrefoNummer(), refExportsBeschreibung);
                            TimelineLogger.info(getClass(), "\t\t\t\t==> " + refExportsBeschreibung);
                        }
                    }
                }
            }
        }
        return targetMap;
    }
*/

}
