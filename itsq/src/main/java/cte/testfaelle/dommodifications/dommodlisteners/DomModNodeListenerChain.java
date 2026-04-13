package cte.testfaelle.dommodifications.dommodlisteners;

import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModLevelInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Implementierung von {@link DomModNodeListener} für das Verketten mehrerer Instanzen
 */
public class DomModNodeListenerChain
        implements DomModNodeListener {

    private final List<DomModNodeListener> chain;

    public DomModNodeListenerChain(DomModNodeListener... listenerArray) {
        List<DomModNodeListener> newList = new ArrayList<>();
        if (listenerArray != null) {
            for (DomModNodeListener listener : listenerArray) {
                if (listener != null) {
                    newList.add(listener);
                }
            }
        }
        this.chain = Collections.unmodifiableList(newList);
    }

    protected List<DomModNodeListener> getChain() {
        return chain;
    }

    @Override
    public boolean acceptNode(DomModContext ctx, DomModLevelInfo levelInfo, Node node, boolean hasNestedNodes, List<ProcessingInstruction> embeddedProcessingInstructions) {
        boolean doRecurse = false;
        for (DomModNodeListener listener : getChain()) {
            doRecurse |= listener.acceptNode(ctx, levelInfo, node, hasNestedNodes, embeddedProcessingInstructions);
        }
        return doRecurse;
    }
}
