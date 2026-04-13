package cte.testfaelle.consistency.parser;

import cte.testfaelle.consistency.RefExportsBeschreibung;
import cte.testfaelle.consistency.RefExportsParserException;
import cte.testfaelle.domain.TestCrefo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Parser für das Erstellen einer {@link RefExportsBeschreibung} aus einer Datei
 */
public class DefaultRefExportsParser implements RefExportsParserIF {
    private static final String TAG_EIGNER_VC = "eigner-vc>";

    @Override
    public RefExportsBeschreibung parseFile(String customerKey, TestCrefo testCrefo) {
        File refExportXmlFile = testCrefo.getItsqRexExportXmlFile();
        String absolutePath = refExportXmlFile.getAbsolutePath();
        try (FileReader fileReader = new FileReader(refExportXmlFile); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            RefExportsBeschreibung.REF_EXPORT_TYPE refExportType = RefExportsBeschreibung.REF_EXPORT_TYPE.UNBEKANNT;
            String crefoString = null;
            String clzString = null;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (refExportType == RefExportsBeschreibung.REF_EXPORT_TYPE.UNBEKANNT) {
                    refExportType = getRefExportType(line);
                }
                if (crefoString == null) {
                    crefoString = getCrefoNummerString(line);
                }
                if (clzString == null) {
                    clzString = getClzString(line);
                }
                if (refExportType != RefExportsBeschreibung.REF_EXPORT_TYPE.UNBEKANNT && crefoString != null && clzString != null) {
                    break;
                }
            } // Schleife über die Zeilen in der Datei
            if (crefoString == null) {
                throw new RefExportsParserException("Crefonummer in den REF-EXports ist nicht gesetzt, " + absolutePath);
                /**} else if (refExportType == RefExportsBeschreibung.REF_EXPORT_TYPE.UNBEKANNT) {
                 throw new RefExportsParserException("Firma/Privatperson/Löschsatz in den REF-EXports ist nicht gesetzt, " + refExportsFile.getAbsolutePath());**/
            } else if (!refExportXmlFile.getName().contains(crefoString)) {
                throw new RefExportsParserException("Crefonummer aus dem Datei-Namen passt nicht zu der Angabe im Datei-Inhalt, " + absolutePath);
            } else {
                return new RefExportsBeschreibung(customerKey, testCrefo, refExportType, clzString);
            }
        } catch (IllegalStateException e) {
            throw new RefExportsParserException(e.getMessage() + "\n" + absolutePath);
        } catch (FileNotFoundException e) {
            throw new RefExportsParserException("Angegebene Datei mit REF-EXports existiert nicht: " + absolutePath);
        } catch (IOException e) {
            throw new RefExportsParserException("Fehler beim Lesen der Datei mit REF-EXports: " + absolutePath);
        }
    }

    @Override
    public String getCrefoNummerString(String line) {
        return null;
    }

    @Override
    public String getClzString(String line) {
        /*
            crm : clz-eigner-vc
            ctc : datenVC, zustaendigerVC
            dfo : eigner-vc
            drd : vcClz
            fsu : clz-eigner-vc
            fw  : clz-eigner-vc
            ika : Daten-VC
            ppa : zustaendiger-daten-vc
            rtn : eigner-vc
            vsX : clz-eigner-vc
            zew : eigner-vc
         */
        return getStringForXmlTag(line, TAG_EIGNER_VC, 3);
    }

    @Override
    public RefExportsBeschreibung.REF_EXPORT_TYPE getRefExportType(String line) {
        return null;
    }

    protected String getStringForXmlTag(String line, String xmlTag, int pos) {
        int posValue;
        String resultStr = null;
        if ((posValue = line.indexOf(xmlTag)) > 0) {
            int start = Math.min(posValue + xmlTag.length(), line.length());
            int end = Math.min(start + pos, line.length());
            resultStr = line.substring(start, end);
        }
        return resultStr;
    }

    protected boolean existsXmlTAG(String line, String xmlTag) {
        return line.indexOf(xmlTag) > 0;
    }

}
