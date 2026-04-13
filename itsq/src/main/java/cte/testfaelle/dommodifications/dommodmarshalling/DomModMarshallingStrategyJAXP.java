package cte.testfaelle.dommodifications.dommodmarshalling;

import cte.testfaelle.dommodifications.dommodcommon.DomModException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implementierung von {@link DomModMarshallingStrategy} mit 'Standard'-DOM. Diese Variante der Implementierung
 * findet man in der Mehrzahl der Beispiele für DOM/JAXP
 *
 * @deprecated Ersatz ist {@link DomModMarshallingStrategyLS}
 */
@Deprecated
public class DomModMarshallingStrategyJAXP
        implements DomModMarshallingStrategy {
    private final DocumentBuilderFactory dbf;
    private final TransformerFactory trf;

    public DomModMarshallingStrategyJAXP() {
        dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        } catch (ParserConfigurationException e) {
            throw new DomModException("FATAL: DOM-Implementierung unterstützt nicht den Schutz vor XXE-Angriffen " + dbf.getClass().getName(), e);
        }
        trf = TransformerFactory.newInstance();
    }

    @Override
    public Document parseFromStream(InputStream is)
            throws DomModException {
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new DomModException(getClass().getSimpleName() + "#parseFromStream scheitert bei der Erzeugung des DocumentBuilders", e);
        } catch (IOException | SAXException e) {
            throw new DomModException(getClass().getSimpleName() + "#parseFromStream scheitert beim Unmarshalling aus einem InputStream", e);
        }
    }

    @Override
    public void writeToStream(Document doc, OutputStream os)
            throws DomModException {
        Transformer transformer;
        try {
            transformer = trf.newTransformer();
            // pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException | IllegalArgumentException e) {
            throw new DomModException(getClass().getSimpleName() + "#writeToStream scheitert bei der Konfiguration des Transformers", e);
        } catch (TransformerException e) {
            throw new DomModException(getClass().getSimpleName() + "#writeToStream scheitert beim Marshalling in einen OutputStream", e);
        }
    }

}
