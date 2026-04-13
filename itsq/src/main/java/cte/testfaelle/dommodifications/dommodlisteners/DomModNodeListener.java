package cte.testfaelle.dommodifications.dommodlisteners;

import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModException;
import cte.testfaelle.dommodifications.dommodcommon.DomModLevelInfo;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Schnittstelle für die Übergabe einzelner DOM-Nodes
 */
public interface DomModNodeListener {
    /**
     * verarbeite einen (weiteren) Knoten im Baum
     *
     * @param ctx                            Context-Container
     * @param levelInfo                      Info zur Verschachtelungstiefe
     * @param node                           aktueller DOM-{@link Node}
     * @param hasNestedNodes                 true, wenn ausser Processing-Instructions weitere Kind-Elemente existieren
     * @param embeddedProcessingInstructions Liste der Processing-Instructions, nullable
     * @return true, wenn die Rekursion eine Ebene tiefer fortgesetzt werden soll
     */
    boolean acceptNode(DomModContext ctx, DomModLevelInfo levelInfo,
                       Node node, boolean hasNestedNodes, List<ProcessingInstruction> embeddedProcessingInstructions)
            throws DomModException;

}
