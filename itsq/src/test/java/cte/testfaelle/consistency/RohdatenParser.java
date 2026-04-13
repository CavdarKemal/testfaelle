package cte.testfaelle.consistency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Parser für das Erstellen einer {@link RohdatenBeschreibung} aus einer Datei
 */
public class RohdatenParser implements RohdatenParserIF {
    protected static final String TAG_CRF = "crefonummer>";
    protected static final String TAG_FIRMA_FALSE = "firma>false<";
    protected static final String TAG_FIRMA_TRUE = "firma>true<";
    protected static final String TAG_CLZ_EIGNER_VC = "eigner-vc>";

    private static long getValueFromXmlTag(File rohdatenDatei, String strTagName, String strValue) {
        if (strValue == null) {
            throw new RohdatenParserException(strTagName + " in den Rohdaten ist nicht gesetzt, " + rohdatenDatei.getAbsolutePath());
        }
        long longValue;
        try {
            longValue = Long.valueOf(strValue);
        } catch (NumberFormatException e) {
            throw new RohdatenParserException(strTagName + " in den Rohdaten ist nicht numerisch: " + strValue);
        }
        if (strTagName.equals("Crefonummer") && !rohdatenDatei.getName().contains(strValue)) {
            throw new RohdatenParserException(strTagName + " n den Rohdaten stimmt nicht überein mit dem Dateinamen: " + strValue + ", Dateiname ist: " + rohdatenDatei.getName());
        }
        return longValue;
    }

    @Override
    public RohdatenBeschreibung parseFile(File rohdatenDatei) {
        if (rohdatenDatei == null) {
            throw new RohdatenParserException("angegebene Datei mit Rohdaten ist null");
        } else {
            boolean steuerungsdatenBeendet = false;
            boolean firmaTrueVorhanden = false;
            boolean firmaFalseVorhanden = false;
            boolean identPPVorhanden = false;
            boolean identFIVorhanden = false;
            try (FileReader frd = new FileReader(rohdatenDatei); BufferedReader buf = new BufferedReader(frd)) {
                Boolean firma = null;
                String crefoString = null;
                String clzEignerVCString = null;
                String line;
                while ((line = buf.readLine()) != null) {
                    int posCrf;
                    if (!steuerungsdatenBeendet) {
                        if (line.contains(TAG_FIRMA_TRUE)) {
                            firmaTrueVorhanden = true;
                            firma = true;
                        }
                        if (line.contains(TAG_FIRMA_FALSE)) {
                            firmaFalseVorhanden = true;
                            firma = false;
                        }
                        if (crefoString == null) {
                            if ((posCrf = line.indexOf(TAG_CRF)) > 0 && line.indexOf("original-crefonummer") < 0) {
                                // Tag für die Crefonummer gefunden
                                int start = Math.min(posCrf + TAG_CRF.length(), line.length());
                                int end = Math.min(start + 10, line.length());
                                crefoString = line.substring(start, end);
                            }
                        }
                        if (clzEignerVCString == null) {
                            if ((posCrf = line.indexOf(TAG_CLZ_EIGNER_VC)) > 0) {
                                // Tag für die Crefonummer gefunden
                                int start = Math.min(posCrf + TAG_CLZ_EIGNER_VC.length(), line.length());
                                int end = Math.min(start + 3, line.length());
                                clzEignerVCString = line.substring(start, end);
                            }
                        }
                    }
                    if (line.contains("</") && line.contains("steuerungsdaten>")) {
                        // Kapitel 'Steuerungsdaten' ist vorhanden
                        steuerungsdatenBeendet = true;
                    }
                    if (line.contains("</") && line.contains("kapitel-firmenidentifikation>")) {
                        // Kapitel 'Firmenidentifikation' ist vorhanden
                        identFIVorhanden = true;
                    }
                    if (line.contains("</") && line.contains("kapitel-personenidentifikation>")) {
                        // Kapitel 'Personenidentifikation' ist vorhanden
                        identPPVorhanden = true;
                    }
                } // Schleife über die Zeilen in der Datei
                if (identPPVorhanden && identFIVorhanden) {
                    // muss immer erfüllt sein!
                    throw new RohdatenParserException("Datei mit Rohdaten beinhaltet Firmen- und Personen-Identifikation, " + rohdatenDatei.getName());
                } else if (firmaTrueVorhanden && firmaFalseVorhanden) {
                    // muss immer erfüllt sein!
                    throw new RohdatenParserException("Datei mit Rohdaten beinhaltet 'firma=true' und 'firma=false', " + rohdatenDatei.getName());
                } else if (!steuerungsdatenBeendet) {
                    // hier steigen wir aus, falls kein AB30 vorliegt
                    return null;
                } else if (firma == null) {
                    // muss nur bei AB30-XMLs erfüllt sein
                    throw new RohdatenParserException("Firma/Privatperson in den Rohdaten ist nicht gesetzt, " + rohdatenDatei.getAbsolutePath());
                } else if ((firma && identPPVorhanden) || (!firma && identFIVorhanden)) {
                    // muss nur bei AB30-XMLs erfüllt sein
                    throw new RohdatenParserException("Ident-Kapitel passt nicht zum Status Firma/Privatperson, " + rohdatenDatei.getAbsolutePath());
                } else if (!rohdatenDatei.getName().contains(crefoString)) {
                    // muss nur bei AB30-XMLs erfüllt sein
                    throw new RohdatenParserException("Crefonummer aus dem Datei-Namen passt nicht zu der Angabe im Datei-Inhalt, " + rohdatenDatei.getAbsolutePath());
                } else {
                    return createBeschreibung(rohdatenDatei, crefoString, clzEignerVCString, firma);
                }
            } catch (FileNotFoundException e) {
                throw new RohdatenParserException("angegebene Datei mit Rohdaten existiert nicht: " + rohdatenDatei.getName());
            } catch (IOException e) {
                throw new RohdatenParserException("Fehler beim Lesen der Datei mit Rohdaten: " + rohdatenDatei.getName());
            }
        }
    }

    protected RohdatenBeschreibung createBeschreibung(File rohdatenDatei, String crefoString, String clzEignerVcString, boolean firma) {
        long crf = getValueFromXmlTag(rohdatenDatei, "Crefonummer", crefoString);
        long clzEignerVC = getValueFromXmlTag(rohdatenDatei, "clz-eigner-vc", clzEignerVcString);
        return new RohdatenBeschreibung(rohdatenDatei, crf, clzEignerVC, firma);
    }

}
