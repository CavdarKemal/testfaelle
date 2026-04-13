package cte.testfaelle.dommodifications.dommodlisteners;

import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModLevelInfo;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Implementierung von {@link DomModNodeListener} für das Verifizieren der verwendeten Processing-Instructions.
 * Es geht ausdrücklich _nicht_ darum, vor dem Aufruf von {@link DomModNodeListenerInstructionProcessor} zusätzliche
 * Prüfungen durchzuführen. Stattdessen bietet diese Klasse die Möglichkeit, Quelldaten auf eine korrekte Verwendung
 * von Processing-Instructions zu prüfen ohne diese gleichzeitig zu modifizieren.
 */
public class DomModNodeListenerInstructionValidator
        extends DomModNodeListenerInstructionAbstract {

    private int instructionsFound;

    public DomModNodeListenerInstructionValidator() {
        this(false);
    }

    public DomModNodeListenerInstructionValidator(boolean checkPreconditions) {
        super((checkPreconditions) ? LI_MODE.DETECT_CHECK_NOPROCESS : LI_MODE.DETECT_ONLY);
    }

    /**
     * lese die Gesamt-Zahl aller mit dieser Instanz gefundenen Processing-Instructions
     */
    public int getInstructionsFound() {
        return instructionsFound;
    }

    public boolean acceptNode(DomModContext ctx,
                              DomModLevelInfo levelInfo, Node node, boolean hasNestedNodes, List<ProcessingInstruction> embeddedProcessingInstructions) {
        final boolean doRecurse = isRecursionNecessary(node, hasNestedNodes);
        this.instructionsFound += acceptNodeInternal(ctx, levelInfo, node, embeddedProcessingInstructions);
        return doRecurse;
    }

}
