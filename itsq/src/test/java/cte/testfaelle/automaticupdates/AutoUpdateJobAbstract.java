package cte.testfaelle.automaticupdates;

import cte.testfaelle.domain.TimelineLogger;
import de.creditreform.crefoteam.cte.archivbestand30.util.AB30JaxbUtil;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.Archivbestand;
import de.creditreform.crefoteam.cte.archivbestand30.xmlbinding.ObjectFactory;
import de.creditreform.crefoteam.cte.jaxbbasics.jaxbutil.CteJaxbBasics;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBElement;
import org.junit.Assert;

/**
 * abstrakte Basis-Klasse für das automatische Überarbeiten von Testfällen
 */
public class AutoUpdateJobAbstract {

    protected static final ObjectFactory AB30_OBJECT_FACTORY = new ObjectFactory();
    protected static final AB30JaxbUtil4Test AB30_JAXB_UTIL = new AB30JaxbUtil4Test();
    protected static final Pattern PATTERN_AB30_XML = Pattern.compile("[1-9][0-9]{9}.xml", Pattern.CASE_INSENSITIVE);

    public AutoUpdateJobAbstract() {
    }

    protected void autoUpdate(File parentDir, UpdFn updateFunction) throws Exception {
        Assert.assertNotNull(parentDir);
        Assert.assertTrue(parentDir.exists());
        Assert.assertNotNull(updateFunction);

        int anzAB30gelesen = 0;
        int anzAB30veraendert = 0;
        for (File f : parentDir.listFiles()) {
            String fileName = f.getName();
            if (f.isFile() && PATTERN_AB30_XML.matcher(fileName).matches()) {
                anzAB30gelesen++;
                Archivbestand ab30Updated;
                // Archivbestand einlesen und an die Update-Function verfüttern
                try (InputStream is = new FileInputStream(f);
                     BufferedInputStream bis = new BufferedInputStream(is)) {
                    Archivbestand ab30 = AB30_JAXB_UTIL.unmarshal(bis);
                    // hier gibt es bewusst keine Prüfung 'ab30!=null'
                    ab30Updated = updateFunction.apply(ab30);
                }
                // Ein Ergebnis ungleich null bedeutet, dass wir die Daten wegschreiben müssen...
                if (ab30Updated != null) {
                    CteJaxbBasics cteJaxbBasics = AB30_JAXB_UTIL.getAb30JaxbBasics();
                    anzAB30veraendert++;
                    try (FileOutputStream fos = new FileOutputStream(f);
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        JAXBElement<Archivbestand> jaxbElement = AB30_OBJECT_FACTORY.createArchivbestand(ab30Updated);
                        cteJaxbBasics.marshalXML(null, null)
                                .addNamespacePrefix("arc", "http://www.creditreform.de/crefoteam/archivbestandv3_0")
                                .toStream(bos, jaxbElement);
                    }
                }

            }
        }
        TimelineLogger.info(getClass(), "Anz AB30 gelesen: {}", anzAB30gelesen);
        TimelineLogger.info(getClass(), "Anz AB30 verändert: {}", anzAB30veraendert);
        updateFunction.logResults();
    }

    protected static class AB30JaxbUtil4Test extends AB30JaxbUtil {
        @Override
        public CteJaxbBasics getAb30JaxbBasics() {
            return super.getAb30JaxbBasics();
        }
    }

}
