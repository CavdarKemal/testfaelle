package cte.testfaelle.dommodifications.dommodmarshalling;

import cte.testfaelle.dommodifications.dommodcommon.DomModException;
import java.io.InputStream;
import java.io.OutputStream;
import org.w3c.dom.Document;

/**
 * Schnittstelle zur Nutzung verschiedener Varianten des Marshallings
 */
public interface DomModMarshallingStrategy {

    Document parseFromStream(InputStream in)
            throws DomModException;

    void writeToStream(Document doc, OutputStream out)
            throws DomModException;

}
