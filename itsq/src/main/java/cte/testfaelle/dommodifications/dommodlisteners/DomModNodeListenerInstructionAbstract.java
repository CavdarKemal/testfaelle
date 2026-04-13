package cte.testfaelle.dommodifications.dommodlisteners;

import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModExceptionUnknownInstruction;
import cte.testfaelle.dommodifications.dommodcommon.DomModLevelInfo;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Package-Interne Basisklasse für die Verarbeitung von Processing-Instructions
 */
abstract class DomModNodeListenerInstructionAbstract
        implements DomModNodeListener {

    private static final Map<String, DomModInstruction> INSTRUCTION_MAP;

    static {
        Map<String, DomModInstruction> map = new HashMap<>();
        for (DomModInstruction instruction : DomModInstruction.values()) {
            map.put(instruction.name(), instruction);
        }
        INSTRUCTION_MAP = Collections.unmodifiableMap(map);
    }

    private final LI_MODE mode;

    protected DomModNodeListenerInstructionAbstract(LI_MODE mode) {
        this.mode = mode;
    }

    /**
     * Erstelle eine Liste mit den Standard-Processing-Instructions. Diese wird verwendet, um die
     * Prüfung der verwendeten Instruktionen zu steuern.
     * Hier werden nicht alle Instructions ausgegeben, sondern nur die geprüften/zulässigen/fertigen
     */
    List<DomModInstruction> getStandardInstructions() {
        //
        return Arrays.asList(DomModInstruction.MAX_AGE_YEARS, DomModInstruction.SET_AGE_YEARS);
    }

    protected boolean isRecursionNecessary(Node node, boolean hasNestedNodes) {
        // Wir setzen die Rekursion nur für Element-Nodes mit Kind-Elementen fort...
        return (node.getNodeType() == Node.ELEMENT_NODE) && hasNestedNodes;
    }

    protected int acceptNodeInternal(DomModContext ctx, DomModLevelInfo levelInfo,
                                     Node node, List<ProcessingInstruction> embeddedProcessingInstructions) {
        int istructionsFound = 0;
        if (embeddedProcessingInstructions != null && !embeddedProcessingInstructions.isEmpty()) {
            String nodeName = node.getNodeName();
            String nodeContent = node.getTextContent();
            for (ProcessingInstruction pi : embeddedProcessingInstructions) {
                // Passende Instruction ermitteln
                DomModInstruction instruction = getInstruction(pi);
                // Vor-Bedingungen prüfen...
                String param = null;
                if (mode.isCheckPreconditions()) {
                    param = pi.getData();
                    for (DomModFormatRequirement requirement : instruction.getFormatRequirements()) {
                        requirement.validateNodeContent(nodeName, nodeContent);
                        requirement.validateParam(param);
                    }
                }
                // Instruction auführen
                if (mode.isProcessInstructions()) {
                    instruction.processNode(ctx, levelInfo, node, param);
                }
                istructionsFound++;
            }
        }
        return istructionsFound;
    }

    protected DomModInstruction getInstruction(ProcessingInstruction pi)
            throws DomModExceptionUnknownInstruction {
        String target = pi.getTarget();
        DomModInstruction fromMap = INSTRUCTION_MAP.get(target);
        if (fromMap == null) {
            throw new DomModExceptionUnknownInstruction(target);
        }
        return fromMap;
    }

    protected enum LI_MODE {
        DETECT_ONLY(false, false),
        DETECT_CHECK_NOPROCESS(true, false),
        DETECT_CHECK_PROCESS(true, true);

        private final boolean checkPreconditions;
        private final boolean processInstructions;

        LI_MODE(boolean checkPreconditions, boolean processInstructions) {
            this.checkPreconditions = checkPreconditions;
            this.processInstructions = processInstructions;
        }

        public boolean isCheckPreconditions() {
            return checkPreconditions;
        }

        public boolean isProcessInstructions() {
            return processInstructions;
        }

    }

}
