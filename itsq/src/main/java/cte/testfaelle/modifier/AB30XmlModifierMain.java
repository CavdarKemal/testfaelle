package cte.testfaelle.modifier;

import cte.testfaelle.domain.DateTimeFinderFunction;
import cte.testfaelle.domain.DateTimeModifierFunction;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;

public class AB30XmlModifierMain {
    private final String outputDirname;
    private List<String> xmlTagsList;

    public AB30XmlModifierMain(String outputDirname, String xmlTagsListFilename) {
        this.outputDirname = outputDirname;
        xmlTagsList = Collections.emptyList();
        if (xmlTagsListFilename != null && !xmlTagsListFilename.isEmpty()) {
            File xmlTagsListFile = new File(xmlTagsListFilename);
            if (xmlTagsListFile.exists()) {
                try {
                    xmlTagsList = FileUtils.readLines(xmlTagsListFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static List<String> getInputDirnamesFromArg(String arg) {
        String[] inputDirnamesAry = arg.split("[, ]");
        List<String> inputDirnamesList = new ArrayList<>();
        for (String inputDirname : inputDirnamesAry) {
            File inputDir = new File(inputDirname);
            if (!inputDir.exists()) {
                TimelineLogger.error(AB30XmlModifierMain.class, "Das Input-Verzeichnis " + inputDirname + " existiert nicht!");
                System.exit(-1);
            }
            if (!inputDir.isDirectory()) {
                TimelineLogger.error(AB30XmlModifierMain.class, inputDirname + " ist kein Verzeichnis!");
                System.exit(-1);
            }
            inputDirnamesList.add(inputDirname);
        }
        return inputDirnamesList;
    }

    public static void main(String[] args) throws IOException {
        File workDir = new File(System.getProperty("user.dir"));
        if (!TimelineLogger.configure(workDir, "AB30Modifier.log", "AB30Modifier-Actions.log")) {
            throw new RuntimeException("Exception beim Konfigurieren der LOG-Dateien!\n");
        }
        List<String> inputDirnamesList = Arrays.asList(TestSupportClientKonstanten.REF_EXPORTS_PHASE_1, TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2, TestSupportClientKonstanten.REF_EXPORTS_ROOT);
        String outputDirname = "MODIFIED";
        String xmlTagsListFilename = null;
        if (args.length > 0) {
            inputDirnamesList = getInputDirnamesFromArg(args[0]);
        }
        if (args.length > 1) {
            outputDirname = args[1];
        }
        if (args.length > 2) {
            xmlTagsListFilename = args[2];
        }
        File outputDir = new File(outputDirname);
        if (outputDir.exists()) {
            TimelineLogger.error(AB30XmlModifierMain.class, "Das Output-Verzeichnis " + outputDirname + " existiert bereits!");
            //System.exit(-1);
        }
        outputDir.mkdirs();
        AB30XmlModifierMain ab30XmlModifierMain = new AB30XmlModifierMain(outputDirname, xmlTagsListFilename);
        ab30XmlModifierMain.doWork(inputDirnamesList);
        TimelineLogger.close();
    }

    public void doWork(List<String> inputDirnamesList) throws IOException {
        DateTimeFinderFunction dateTimeFinderFunction = new DateTimeFinderFunction(xmlTagsList, Arrays.asList(TestSupportClientKonstanten.DATE_PATTERN, TestSupportClientKonstanten.DATE_TIME_PATTERN));
        for (String inputDirname : inputDirnamesList) {
            TimelineLogger.info(AB30XmlModifierMain.class, "==========================================================================================================");
            TimelineLogger.info(AB30XmlModifierMain.class, "Verarbeite die Dateien im Verzeichnis '" + inputDirname + "' und speichere sie nach '" + outputDirname + "'...");
            Set<String> xmlTagsSet = new HashSet<>();
            try (Stream<Path> paths = Files.walk(Paths.get(inputDirname))) {
                paths.filter(path -> path.toFile().getName().endsWith(".xml"))
                        .forEach(path -> {
                            File inputXmlFile = path.toFile();
                            DateTimeModifierFunction dateTimeModifierFunction = new DateTimeModifierFunction(getLastModifiedDate(inputXmlFile));
                            int diffOfDays = dateTimeModifierFunction.getDiffOfDays(new Date(), dateTimeModifierFunction.getLasdtModifiedDate());
                            TimelineLogger.info(AB30XmlModifierMain.class, "\tVerarbeite die Datei '" + inputXmlFile.getAbsolutePath() + "'");
                            TimelineLogger.info(AB30XmlModifierMain.class, "\t\tDateidatum: " + TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.format(dateTimeModifierFunction.getLasdtModifiedDate()) +
                                    ",   Tagesdatum: " + TestSupportClientKonstanten.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.format(new Date()));
                            if (diffOfDays > 0) {
                                File outputXmlFile = new File(outputDirname, inputXmlFile.getPath());
                                outputXmlFile.getParentFile().mkdirs();
                                TimelineLogger.info(AB30XmlModifierMain.class, "\t\t==> es werden jedem Datum " + diffOfDays + " Tage addiert...");
                                try {
                                    AB30XmlModifier ab30XmlModifier = new AB30XmlModifier(inputXmlFile, outputXmlFile);
                                    Set<String> tmpSet = ab30XmlModifier.modifyXml(dateTimeFinderFunction, dateTimeModifierFunction);
                                    xmlTagsSet.addAll(tmpSet);
                                } catch (Exception ex) {
                                    TimelineLogger.error(AB30XmlModifierMain.class, ex.getMessage());
                                    ex.printStackTrace();
                                }
                            } else {
                                TimelineLogger.warn(AB30XmlModifierMain.class, "\t\t-->Datei wird übersprungen, da sie keinen Tag alt ist.");
                            }
                        });

            }
            writeXmlTagsSet(inputDirname, xmlTagsSet);
            TimelineLogger.info(AB30XmlModifierMain.class, "----------------------------------------------------------------------------------------------------------\n");
            TimelineLogger.info(AB30XmlModifierMain.class, "LOG-Ausgaben in der Datei '/ITSQ.Testfaelle-CTE/log4j.properties'\n");
        }
    }

    private Date getLastModifiedDate(File inputXmlFile) {
        try {
            return DateUtils.parseDate("23.01.2021", new String[]{"dd.MM.yyyy"});
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return new Date();
    }

    private void writeXmlTagsSet(String inputDirname, Set<String> xmlTagsSet) {
        try {
            File file = new File(inputDirname + "-XML-Tags.txt");
            List<String> xmlTagsList = new ArrayList<>();
            xmlTagsList.addAll(xmlTagsSet);
            Collections.sort(xmlTagsList);
            FileUtils.writeLines(file, xmlTagsList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
