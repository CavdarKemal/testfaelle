package cte.testfaelle.dommodifications.dommodlisteners;

import cte.testfaelle.domain.TimelineLogger;
import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModException;
import cte.testfaelle.dommodifications.dommodcommon.DomModLevelInfo;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Implementierung von {@link DomModNodeListener} für das Logging der Baumstruktur
 */
public class DomModNodeListenerLog implements DomModNodeListener {

    protected static final String ACCEPTED_AS_BLANKS = "\r\n \t";

    public DomModNodeListenerLog() {
        // no-arg constructor should be available
    }

    protected String renderProcessingInstructions(List<ProcessingInstruction> piList) {
        if (piList == null || piList.isEmpty()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(" [");
            for (ProcessingInstruction pi : piList) {
                sb.append(pi.getTarget()).append('/').append(pi.getData()).append(';');
            }
            sb.setLength(sb.length() - 1);
            sb.append("]");
            return sb.toString();
        }
    }

    public boolean acceptNode(DomModContext ctx,
                              DomModLevelInfo levelInfo, Node node, boolean hasNestedNodes, List<ProcessingInstruction> embeddedProcessingInstructions) {
        final String renderedText;
        final boolean doRecurse;
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE: {
                doRecurse = hasNestedNodes;
                if (hasNestedNodes) {
                    renderedText = "ELEMENT named '" + node.getNodeName() + "' (has children)";
                } else {
                    renderedText = "ELEMENT named '" + node.getNodeName() + "' containing: '" + node.getTextContent() + "'" + renderProcessingInstructions(embeddedProcessingInstructions);
                }
                break;
            }
            case Node.ATTRIBUTE_NODE: {
                renderedText = "ATTRIBUTE named ':" + node.getNodeName() + "'";
                doRecurse = false;
                break;
            }
            case Node.TEXT_NODE: {
                if (isBlanksOnly(node)) {
                    renderedText = "TEXT: linefeeds/spaces only";
                } else {
                    renderedText = "TEXT: " + node.getTextContent();
                }
                doRecurse = false;
                break;
            }
            case Node.CDATA_SECTION_NODE: {
                renderedText = "CDATA";
                doRecurse = false;
                break;
            }
            case Node.ENTITY_REFERENCE_NODE: {
                renderedText = "ENTITY-REF:";
                doRecurse = false;
                break;
            }
            case Node.COMMENT_NODE: {
                renderedText = "COMMENT";
                doRecurse = false;
                break;
            }
            case Node.DOCUMENT_FRAGMENT_NODE: {
                renderedText = "DOC-FRAGMENT";
                doRecurse = false;
                break;
            }
            case Node.DOCUMENT_NODE: {
                renderedText = "DOC-NODE:";
                doRecurse = false;
                break;
            }
            case Node.DOCUMENT_TYPE_NODE: {
                renderedText = "DOC-TYPE";
                doRecurse = false;
                break;
            }
            case Node.ENTITY_NODE: {
                renderedText = "ENTITY";
                doRecurse = false;
                break;
            }
            case Node.PROCESSING_INSTRUCTION_NODE: {
                ProcessingInstruction pi = (ProcessingInstruction) node;
                renderedText = "PROC-INSTRUCTION target '" + pi.getTarget() + "' / data '" + pi.getData() + "'";
                doRecurse = false;
                break;
            }
            case Node.NOTATION_NODE: {
                renderedText = "NOTATION";
                doRecurse = false;
                break;
            }
            default:
                throw new DomModException("Type of DOM-Node not supported: " + node.getNodeType());
        }
        TimelineLogger.info(getClass(), "{}{}", levelInfo.getIndent(), renderedText);
        return doRecurse;
    }

    protected boolean isBlanksOnly(Node node) {
        for (char c : node.getTextContent().toCharArray()) {
            if (ACCEPTED_AS_BLANKS.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }

}
