package cte.testfaelle.consistency;

import de.creditreform.crefoteam.cte.archivbestand30.util.AB30JaxbUtil;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Archivbestand;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.KapitelFirmenbeteiligte;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.KapitelKonzernzugehoerigkeit;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.KapitelNegativmerkmale;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Steuerungsdaten;
import de.creditreform.crefoteam.cte.jaxbbasics.jaxbbasicscommon.JaxbBasicsRuntimeException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;

class RohdatenParserJaxb implements RohdatenParserIF {
    private final List<String> excludedFiles;
    private final AB30JaxbUtil ab30JaxbUtil;

    public RohdatenParserJaxb(AB30JaxbUtil ab30JaxbUtil) {
        this.ab30JaxbUtil = ab30JaxbUtil;
        this.excludedFiles = Arrays.asList("befreiung.xml",
                "bilanz.xml",
                "bilanz_befreiung.xml",
                "EH-Produktauftrag-ABLEHNUNG_FIRMA_FIRMA.xml",
                "EH-Produktauftrag-ABLEHNUNG_FIRMA_PRIVPERSON.xml",
                "EH-Produktauftrag-ABLEHNUNG_PRIVPERSON_FIRMA.xml",
                "EH-Produktauftrag-ABLEHNUNG_PRIVPERSON_PRIVPERSON.xml",
                "EH-Produktauftrag-ERLEDIGUNG_FIRMA_FIRMA.xml",
                "EH-Produktauftrag-ERLEDIGUNG_FIRMA_PRIVPERSON.xml",
                "EH-Produktauftrag-ERLEDIGUNG_PRIVPERSON_FIRMA.xml",
                "EH-Produktauftrag-ERLEDIGUNG_PRIVPERSON_PRIVPERSON.xml",
                "TestCrefos.properties"
        );
    }

    @Override
    public RohdatenBeschreibung parseFile(File rohdatenDatei) {
        if (rohdatenDatei == null || excludedFiles.contains(rohdatenDatei.getName())) {
            return null;
        }
        try (InputStream fis = new FileInputStream(rohdatenDatei);
             BufferedInputStream buf = new BufferedInputStream(fis)) {
            byte[] xmlDaten = IOUtils.toByteArray(buf);
            Archivbestand ab30 = ab30JaxbUtil.unmarshal(xmlDaten);
            Steuerungsdaten stDaten = ab30.getSteuerungsdaten();
            if (stDaten == null) {
                throw new RohdatenParserException("angegebene Datei mit Rohdaten enth\u00e4lt nicht das Kapitel 'steuerungsdaten': " + rohdatenDatei.getName());
            }
            final long crefo = stDaten.getCrefonummer().longValue();
            final long clzEignerVC = stDaten.getClzEignerVc().longValue();
            final boolean fiErkannt = ab30.getKapitelFirmenidentifikation() != null;
            final boolean ppErkannt = ab30.getKapitelPersonenidentifikation() != null;
            if (fiErkannt && ppErkannt) {
                throw new RohdatenParserException("angegebene Datei mit Rohdaten enth\u00e4lt Kapitel f\u00fcr Firma UND Privatperson: " + rohdatenDatei.getName());
            }
            // Wir beginnen mit den Firmen-Beteiligten
            List<Long> beteiligte = new ArrayList<>();
            KapitelFirmenbeteiligte kapBtlg = ab30.getKapitelFirmenbeteiligte();
            if (kapBtlg != null) {
                for (KapitelFirmenbeteiligte.Firmenbeteiligter b : kapBtlg.getFirmenbeteiligter()) {
                    beteiligte.add(b.getCrefonummerBeteiligter().longValue());
                }
            }
            // ...dicht gefolgt von der Konzernmutter...
            KapitelKonzernzugehoerigkeit kapKonzern = ab30.getKapitelKonzernzugehoerigkeit();
            if (kapKonzern != null && kapKonzern.getCrefonummer() != null) {
                beteiligte.add(kapKonzern.getCrefonummer().longValue());
            }
            // ...es folgen die Daten aus Negativmerkmalen
            KapitelNegativmerkmale kapNeg = ab30.getKapitelNegativmerkmale();
            if (kapNeg != null) {
                for (KapitelNegativmerkmale.Negativmerkmal neg : kapNeg.getNegativmerkmal()) {
                    if (neg.getCrefonummerUebertragenVon() != null) {
                        beteiligte.add(neg.getCrefonummerUebertragenVon().longValue());
                    }
                    if (neg.getCrefonummerVeroeffentlicht() != null) {
                        beteiligte.add(neg.getCrefonummerVeroeffentlicht().longValue());
                    }
                    KapitelNegativmerkmale.Negativmerkmal.Verfahrensbeteiligter vBtlg = neg.getVerfahrensbeteiligter();
                    if (vBtlg != null && vBtlg.getCrefonummer() != null) {
                        beteiligte.add(vBtlg.getCrefonummer().longValue());
                    }
                }
            }
            return new RohdatenBeschreibung(rohdatenDatei, crefo, clzEignerVC, fiErkannt, beteiligte);
        } catch (JaxbBasicsRuntimeException e) {
            throw new RohdatenParserException("angegebene Datei mit Rohdaten konnte nicht als AB30 interbretiert werden: " + rohdatenDatei.getName(), e);
        } catch (FileNotFoundException e) {
            throw new RohdatenParserException("angegebene Datei mit Rohdaten existiert nicht: " + rohdatenDatei.getName());
        } catch (IOException e) {
            throw new RohdatenParserException("Fehler beim Lesen der Datei mit Rohdaten: " + rohdatenDatei.getName());
        }
    }

}
