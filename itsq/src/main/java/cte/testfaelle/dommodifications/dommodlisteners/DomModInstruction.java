package cte.testfaelle.dommodifications.dommodlisteners;

import cte.testfaelle.dommodifications.dommodcommon.DomModContext;
import cte.testfaelle.dommodifications.dommodcommon.DomModException;
import cte.testfaelle.dommodifications.dommodcommon.DomModLevelInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Node;

import static cte.testfaelle.dommodifications.dommodlisteners.DomModFormatRequirement.CONTENT_D4;
import static cte.testfaelle.dommodifications.dommodlisteners.DomModFormatRequirement.CONTENT_Y_M_D;
import static cte.testfaelle.dommodifications.dommodlisteners.DomModFormatRequirement.PARAM_EXISTS;
import static cte.testfaelle.dommodifications.dommodlisteners.DomModFormatRequirement.PARAM_NUMERISCH;
import static cte.testfaelle.dommodifications.dommodlisteners.DomModFormatRequirement.PARAM_POSITIV;

/**
 * Package-internes Enum mit den ausführbaren Entsprechungen zu Processing-Instructions im XML
 * Bitte beachten, dass für den ausführenden Java-Code die öffentliche Schnittstelle von
 * {@link DomModNodeListenerInstructionProcessor} bereit gestellt wird. Es soll ein internes
 * Implementierungs-Detail sein und bleiben, dass aktuell die Umsetzung mit Enums erfolgt.
 */
enum DomModInstruction {
    // Der Inhalt des XML-Elementes im Format yyyy-MM-dd muss über mehrere Phasen des Tests hinweg gleich
    // bleiben. Parameter ist der Key, unter dem die Information abgelegt werden soll.
    // MATCH_Y_M_D_SET_OTHER wird verwendet, wenn der Inhalt des Elementes gelesen und auf andere Test-Phasen
    // übertragen werden soll.
    MATCH_Y_M_D_SET_OTHER(CONTENT_Y_M_D, PARAM_EXISTS) {
        @Override
        public void processNode(DomModContext ctx, DomModLevelInfo levelInfo, Node node, String param) {
            String previousContent = ctx.getStoredContent(param);
            if (previousContent != null) {
                throw new DomModException("Content for key '" + param + "' already set to " + previousContent);
            }
            ctx.putStoredContent(param, node.getTextContent().substring(0, 10));
        }
    },
    // Der Inhalt des XML-Elementes im Format yyyy-MM-dd muss über mehrere Phasen des Tests hinweg gleich
    // bleiben. Parameter ist der Key, unter dem die Information abgelegt werden soll.
    // MATCH_Y_M_D_RECEIVE_OTHER wird verwendet, wenn der Inhalt des Elementes mit Werten aus einer anderen Test-Phase
    // überschrieben werden soll.
    MATCH_Y_M_D_RECEIVE_OTHER(CONTENT_Y_M_D, PARAM_EXISTS) {
        @Override
        public void processNode(DomModContext ctx, DomModLevelInfo levelInfo, Node node, String param) {
            String storedContent = ctx.getStoredContent(param);
            if (storedContent == null) {
                throw new DomModException("Content for key '" + param + "' not set");
            } else {
                CONTENT_Y_M_D.validateNodeContent("stored content for key '" + param + "'", storedContent);
            }
            String currentContent = node.getTextContent();
            String newContent;
            if (currentContent.length() <= 10) {
                newContent = storedContent;
            } else {
                newContent = storedContent.substring(0, 10) + currentContent.substring(10);
            }
            node.setTextContent(newContent);
        }
    },

    // Der Inhalt im XML-Element soll in Relation zum Referenz-Datum auf ein festes Alter gebracht werden
    SET_AGE_YEARS(CONTENT_D4, PARAM_NUMERISCH) {
        @Override
        public void processNode(DomModContext ctx, DomModLevelInfo levelInfo, Node node, String param) {
            int ageOffset = Integer.parseInt(param);
            int yearsModified = ctx.getReferenzJahr() - ageOffset;
            setContentPrefix(node, 4, String.valueOf(yearsModified));
        }
    },
    // Der Inhalt im XML-Element darf in Relation zum Referenz-Datum nicht älter sein als X Jahre
    MAX_AGE_YEARS(CONTENT_D4, PARAM_POSITIV) {
        @Override
        public void processNode(DomModContext ctx, DomModLevelInfo levelInfo, Node node, String param) {
            int ageOffset = Integer.parseInt(param);
            int yearsModified = ctx.getReferenzJahr() - ageOffset;
            int yearsCurrently = Integer.parseInt(node.getTextContent().substring(0, 4));
            if (yearsCurrently < yearsModified) {
                setContentPrefix(node, 4, String.valueOf(yearsModified));
            }
        }
    };

    private final List<DomModFormatRequirement> formatRequirements;

    DomModInstruction(DomModFormatRequirement... reqArray) {
        if (reqArray == null || reqArray.length == 0) {
            formatRequirements = Collections.emptyList();
        } else {
            List<DomModFormatRequirement> reqList = new ArrayList<>();
            for (DomModFormatRequirement req : reqArray) {
                if (req != null) {
                    reqList.add(req);
                }
            }
            formatRequirements = Collections.unmodifiableList(reqList);
        }
    }

    /**
     * Lese die Liste der Vorbedingungen für Element-Inhalt und Processing-Parameter
     */
    public List<DomModFormatRequirement> getFormatRequirements() {
        return formatRequirements;
    }

    /**
     * Führe die aktuelle {@link DomModInstruction} auf dem angegebenen {@link Node} aus. Vor dem Aufruf dieser Methode
     * wurden die Vorbedingungen geprüft.
     *
     * @param ctx       Context-Container
     * @param levelInfo Info zur Verschachtelungstiefe
     * @param node      zu verrändernder {@link Node}
     * @param param     Parameter der Processing-Instruction
     */
    public abstract void processNode(DomModContext ctx, DomModLevelInfo levelInfo, Node node, String param);

    /**
     * Setze für den angegebenen Knoten einen neuen Inhalt. Um dabei eventuelle Processing-Instructions zu erhalten,
     * erfolgt der Zugriff über den Kin-Knoten vom Typ 'TEXT'
     *
     * @param node       zu verändernder {@link Node}
     * @param newContent neuer Inhalt
     */
    protected void setContent(Node node, String newContent) {
        Node child = node.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.TEXT_NODE) {
                child.setTextContent(newContent);
                return;
            }
            child = child.getNextSibling();
        }
        throw new DomModException("Child with text-content not found for node named: " + node.getTextContent());
    }

    /**
     * Erzetze die ersten X Zeichen im Inhalt des Elementes durch den neuen Text
     *
     * @param node         zu verändernder {@link Node}
     * @param cutOffLength Anzahl der abzuschneidenden Zeichen
     * @param newPrefix    neuer Prefix für den Inhalt
     */
    protected void setContentPrefix(Node node, int cutOffLength, String newPrefix) {
        setContent(node, newPrefix + node.getTextContent().substring(cutOffLength));
    }

}
