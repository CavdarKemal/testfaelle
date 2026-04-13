package cte.testfaelle.dommodifications.dommodlisteners;

import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModLevelInfo;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Implementierung von {@link DomModNodeListener} für das Abarbeiten von Processing-Instructions
 */
public class DomModNodeListenerInstructionProcessor
        extends DomModNodeListenerInstructionAbstract {

    public DomModNodeListenerInstructionProcessor() {
        super(LI_MODE.DETECT_CHECK_PROCESS);
    }

    public boolean acceptNode(DomModContext ctx, DomModLevelInfo levelInfo,
                              Node node, boolean hasNestedNodes, List<ProcessingInstruction> embeddedProcessingInstructions) {
        final boolean doRecurse = isRecursionNecessary(node, hasNestedNodes);
        acceptNodeInternal(ctx, levelInfo, node, embeddedProcessingInstructions);
        return doRecurse;
    }

}
