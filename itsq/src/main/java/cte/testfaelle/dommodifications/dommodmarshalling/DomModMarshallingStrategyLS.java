package cte.testfaelle.dommodifications.dommodmarshalling;

import cte.testfaelle.dommodifications.dommodcommon.DomModException;
import java.io.InputStream;
import java.io.OutputStream;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSSerializer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implementierung von {@link DomModMarshallingStrategy} mit {@link DOMImplementationLS}, vermeidet überzählige
 * Zeilenvorschübe in der Ausgabe
 */
public class DomModMarshallingStrategyLS
        implements DomModMarshallingStrategy {
    private final DOMImplementationLS implLS;

    public DomModMarshallingStrategyLS()
            throws DomModException {
        try {
            implLS = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
            throw new DomModException("Konstruktor " + getClass().getSimpleName() + " scheitert bei der Erzeugung von DOMImplementationLS");
        }
    }

    @Override
    public Document parseFromStream(InputStream in)
            throws DomModException {
        try {
            LSInput lsInput = implLS.createLSInput();
            lsInput.setEncoding(UTF_8.name());
            lsInput.setByteStream(in);
            LSParser parser = implLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, "http://www.w3.org/2001/XMLSchema");
            // XXE-Schutz: externe Ressourcen (Entities, DTD-Includes) grundsätzlich ablehnen
            LSResourceResolver xxeBlocker = (type, namespaceURI, publicId, systemId, baseURI) -> {
                throw new LSException(LSException.PARSE_ERR,
                        "XXE-Schutz: Externe Ressource abgewiesen (systemId=" + systemId + ")");
            };
            parser.getDomConfig().setParameter("resource-resolver", xxeBlocker);
            return parser.parse(lsInput);
        } catch (DOMException | LSException e) {
            throw new DomModException(getClass().getSimpleName() + "#parseFromStream scheitert beim Unmarshalling aus einem InputStream", e);
        }

    }

    @Override
    public void writeToStream(Document doc, OutputStream os)
            throws DomModException {
        try {
            final LSSerializer serializer = implLS.createLSSerializer();
            serializer.setNewLine("\n");
            final LSOutput destination = implLS.createLSOutput();
            destination.setEncoding(UTF_8.name());
            destination.setByteStream(os);
            serializer.write(doc, destination);
        } catch (LSException e) {
            throw new DomModException(getClass().getSimpleName() + "#writeToStream scheitert beim Marshalling in einen OutputStream", e);
        }
    }
}
