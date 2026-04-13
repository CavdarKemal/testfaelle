package cte.testfaelle;


import com.github.sisyphsu.dateparser.DateParserUtils;
import cte.testfaelle.domain.TestSupportClientKonstanten;
import cte.testfaelle.domain.TimelineLogger;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ModifyXmlDomParser {

    public static Document readXMLDocumentFromFile(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        return document;
    }

    public static void main(String[] args) throws Exception {
        File workDir = new File(System.getProperty("user.dir"));
        if (!TimelineLogger.configure(workDir, "ModifyXml.log", "ModifyXml-Actions.log")) {
            throw new RuntimeException("Exception beim Konfigurieren der LOG-Dateien!\n");
        }
        List<File> ab30XmlsList1 = getFiles(TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_1);
        List<File> ab30XmlsList2 = getFiles(TestSupportClientKonstanten.ARCHIV_BESTAND_PHASE_2);
        List<File> allFiles = Stream.concat(ab30XmlsList1.stream(), ab30XmlsList2.stream()).collect(Collectors.toList());
        //
        //Achtung: keine ab-Xmls, spaeter vielleicht dazu nehmen
        //List<File> files3 = getFiles("/REF-EXPORTS/");
        //List<File> allFiles = Stream.concat(Stream.concat(ab30XmlsList1.stream(), ab30XmlsList2.stream())
        // .collect(Collectors.toList()).stream(), files3.stream()).collect(Collectors.toList());
        //
        Date now = new Date();
        List<String> lines = new ArrayList<>();
        lines.add("Die Vorr\u00e4tigkeit ist in folgenden Dateien nicht gesetzt:");
        for (File file : allFiles) {
            String result = writeXmls(file, now);
            if (result != null) {
                lines.add(result);
            }
        }
        String datenow = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(now);
        if (!lines.isEmpty() && lines.size() > 1) {
            Path file = Paths.get("report-automatisierung-" + datenow + ".txt");
            Files.write(file, lines, StandardCharsets.UTF_8);
        }
        TimelineLogger.close();
    }

    private static String writeXmls(File f, Date date) throws Exception {
        Document document = readXMLDocumentFromFile(f);
        Element steuerungsdaten = (Element) document.getElementsByTagName("arc:steuerungsdaten").item(0);
        if (steuerungsdaten == null) return null;
        Node itemVorraetig = steuerungsdaten.getElementsByTagName("arc:vorraetig-bis").item(0);

// TODO: weitere Felder definieren und modifizieren
//        Node itemLetzteArchivAenderung = steuerungsdaten.getElementsByTagName("arc:letzte-archivaenderung").item(0);
//        if (Objects.nonNull(itemLetzteArchivAenderung)) {
//            Date laae = DateParserUtils.parseDate(itemLetzteArchivAenderung.getTextContent());
//        }

        String result = null;
        if (Objects.nonNull(itemVorraetig)) {
            Date vorr = DateParserUtils.parseDate(itemVorraetig.getTextContent());
            if (date.after(vorr)) {
                Date inFarFuture = DateParserUtils.parseDate("31-11-2100");
                itemVorraetig.setTextContent(new SimpleDateFormat("yyyy-MM-dd").format(inFarFuture));
                writeXml(document, f);
            }
        } else {
            String[] path = f.getPath().replaceAll("\\\\", "/").split("/");
            result = path[path.length - 2] + "/" + path[path.length - 1];
        }
        return result;

    }


    private static List<File> getFiles(String path) {
        List<File> xmlFiles = new ArrayList<>();
        String fileNameWithPath = Paths.get("").toAbsolutePath() + "/" + path;
        sucheDateienRekursiv(new File(fileNameWithPath), xmlFiles);
        return xmlFiles;
    }

    private static void sucheDateienRekursiv(File directory, List<File> xmlFiles) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    sucheDateienRekursiv(file, xmlFiles);
                }
            }
        } else if (directory.isFile() && directory.getName().toLowerCase().endsWith(".xml")) {
            xmlFiles.add(directory);
        }
    }


    private static void writeXml(Document document, File file) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

}
