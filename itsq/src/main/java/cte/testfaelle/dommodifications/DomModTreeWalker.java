package cte.testfaelle.dommodifications;

import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModException;
import cte.testfaelle.dommodifications.dommodcommon.DomModLevelInfo;
import cte.testfaelle.dommodifications.dommodlisteners.DomModNodeListener;
import cte.testfaelle.dommodifications.dommodmarshalling.DomModMarshallingStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * Utility-Klasse für die Iteration durch einen DOM-Tree und die Übergabe der Nodes an einen {@link DomModNodeListener}
 */
public class DomModTreeWalker {

    private final DomModNodeListener nodeListener;

    public DomModTreeWalker(DomModNodeListener nodeListener) {
        this.nodeListener = nodeListener;
    }

    /**
     * Convenience-Variante von {@link #walkDOMTree(DomModContext, Node)}, die das Einlesen der
     * Daten aus einem {@link Path} integriert
     *
     * @param ctx                 Instanz von {@link DomModContext}
     * @param marshallingStrategy Implementierungs-Variante für das DOM-Marshalling/-Unmarshaling
     * @param src                 {@link Path} zur Vorgabe der Quelldaten
     * @return DOM-Document
     */
    public Document walkDOMTree(DomModContext ctx, DomModMarshallingStrategy marshallingStrategy, Path src)
            throws DomModException {
        Document doc;
        if (src == null) {
            throw new DomModException("src cannot be null");
        } else if (Files.exists(src) && Files.isDirectory(src)) {
            throw new DomModException("src must not be a directory");
        }
        // try-finally Blöcke für Lesen und Schreiben sind getrennt, damit ein In-Place-Update möglich wird
        try (InputStream is = Files.newInputStream(src)) {
            doc = marshallingStrategy.parseFromStream(is);
        } catch (IOException e) {
            throw new DomModException(getClass().getSimpleName() + "#walkDOMTree scheitert beim Öffnen des InputStream", e);
        }
        walkDOMTree(ctx, doc);
        return doc;
    }

    /**
     * durchlaufe (rekursiv) den DOM-Baum und übergebe die {@link Node}-Instanzen an den {@link DomModNodeListener}
     *
     * @param ctx  Context-Container
     * @param node Start-Knoten
     */
    public void walkDOMTree(DomModContext ctx, Node node)
            throws DomModException {
        walkDOMTreeInternal(ctx, new DomModLevelInfo(), node);
    }

    protected void walkDOMTreeInternal(DomModContext ctx, DomModLevelInfo levelInfo, Node node)
            throws DomModException {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            final boolean hasNestedNodes;
            final List<ProcessingInstruction> piList;
            if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
                piList = null;
                hasNestedNodes = node.hasChildNodes();
            } else {
                piList = getProcessingInstructions(currentNode);
                hasNestedNodes = (piList == null);
            }
            if (nodeListener.acceptNode(ctx, levelInfo, currentNode, hasNestedNodes, piList)) {
                DomModLevelInfo nestedLevel = levelInfo.createChildContext();
                walkDOMTreeInternal(ctx, nestedLevel, currentNode);
            }
        }

    }

    /**
     * Element-Nodes besitzen 0-N Child-Nodes:
     * a) 0: leeres Element
     * b) 1: Element mit enthaltener Information
     * c1) 2+x: Element mit enthaltener Information (Text) und X Processing-Instructions
     * c2) 2+x: Element mit mehreren Kind-Elementen
     * Diese Methode erkennt den Fall c1 und liefert die Liste der {@link ProcessingInstruction}-Knoten.
     * Für die Fälle a,b und c1 ist der Rückgabewert eine (eventuell leere) Liste, für den Fall
     * c2 ist der Rückgabewert null.
     */
    protected List<ProcessingInstruction> getProcessingInstructions(Node node) {
        Node child = node.getFirstChild();
        int anzTextNodes = 0;
        List<ProcessingInstruction> piList = new ArrayList<>();
        while (child != null) {
            if (child.getNodeType() == Node.TEXT_NODE) {
                anzTextNodes++;
                if (anzTextNodes > 1) {
                    // Es existieren mehrere Kind-Elemente, ProcessingInstructions werden hier nicht ausgewertet
                    return null;
                }
            } else if (child.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
                // Es existieren Kind-Elemente, ProcessingInstructions werden hier nicht ausgewertet
                return null;
            } else {
                piList.add((ProcessingInstruction) child);
            }
            child = child.getNextSibling();
        }
        return piList;
    }

}
