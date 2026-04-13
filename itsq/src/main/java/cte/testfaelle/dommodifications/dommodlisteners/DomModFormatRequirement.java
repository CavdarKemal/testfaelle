package cte.testfaelle.dommodifications.dommodlisteners;

import cte.testfaelle.dommodifications.dommodcommon.DomModException;

/**
 * Package-internes Enum mit den (Format-) Prüfungen für Eingangsdaten und Parameter-Werte
 * Bitte beachten, dass für den ausführenden Java-Code die öffentliche Schnittstelle von
 * {@link DomModNodeListenerInstructionProcessor} bereit gestellt wird. Es soll ein internes
 * Implementierungs-Detail sein und bleiben, das aktuell die Umsetzung mit Enums erfolgt.
 */
enum DomModFormatRequirement {
    /**
     * Parameter der Processing-Instruction ist eine positive Zahl
     */
    PARAM_POSITIV {
        @Override
        public void validateNodeContent(String nodeName, String nodeContent)
                throws DomModException {
            // intentionally empty
        }

        @Override
        public void validateParam(String param)
                throws DomModException {
            // Parameter positiv?
            checkLength("Instruction-parameter", 1, -1, param);
            checkDigits("Instruction-parameter", param);
        }
    },
    /**
     * Parameter der Processing-Instruction ist eine Zahl mit wählbarem Vorzeichen
     */
    PARAM_NUMERISCH {
        @Override
        public void validateNodeContent(String nodeName, String nodeContent)
                throws DomModException {
            // intentionally empty
        }

        @Override
        public void validateParam(String param)
                throws DomModException {
            // Parameter numerisch?
            checkLength("Instruction-parameter", 1, -1, param);
            checkNumeric("Instruction-parameter", param);
        }
    },
    /**
     * Parameter der Processing-Instruction ist ein frei wählbarer Text
     */
    PARAM_EXISTS {
        @Override
        public void validateNodeContent(String nodeName, String nodeContent)
                throws DomModException {
            // intentionally empty
        }

        @Override
        public void validateParam(String param)
                throws DomModException {
            // Parameter numerisch?
            checkLength("Instruction-parameter", 1, -1, param);
        }
    },

    /**
     * Text beginnt mit Datums-Angabe im Format yyyy-MM-dd
     */
    CONTENT_Y_M_D {
        @Override
        public void validateNodeContent(String nodeName, String nodeContent) {
            // Text im Node gefüllt und beginnt mit 4 Ziffern?
            checkLength("Date-Node", 10, -1, nodeContent);
            checkDigits("Date-Node YYYY", nodeContent.substring(0, 4));
            checkDigits("Date-Node MM", nodeContent.substring(5, 7));
            checkDigits("Date-Node DD", nodeContent.substring(8, 10));
        }

        @Override
        public void validateParam(String param)
                throws DomModException {
            // intentionally empty
        }

    },

    /**
     * Text beginnt mit 4 Ziffern
     */
    CONTENT_D4 {
        @Override
        public void validateNodeContent(String nodeName, String nodeContent) {
            // Text im Node gefüllt und beginnt mit 4 Ziffern?
            checkLength("Node", 4, -1, nodeContent);
            checkDigits("Node", nodeContent.substring(0, 4));
        }

        @Override
        public void validateParam(String param)
                throws DomModException {
            // intentionally empty
        }

    },

    /**
     * Text ist eine exakt 3-stellige, positiive Zahl ohne Vorzeichen
     */
    CONTENT_D3 {
        @Override
        public void validateNodeContent(String nodeName, String nodeContent) {
            // Text im Node gefüllt und beginnt mit 4 Ziffern?
            checkLength("Node", 3, 3, nodeContent);
            checkDigits("Node", nodeContent);
        }

        @Override
        public void validateParam(String param)
                throws DomModException {
            // intentionally empty
        }

    },

    /**
     * Text ist eine 3-stellige Zahl zwischen 100 und 999 ohne Vorzeichen, Parameter wird nicht geprüft
     */
    CONTENT_CLZ {
        @Override
        public void validateNodeContent(String nodeName, String nodeContent) {
            // Text im Node gefüllt und beginnt mit 4 Ziffern?
            checkLength("Node", 3, 3, nodeContent);
            checkDigits("Node", nodeContent);
            if (nodeContent.startsWith("0")) {
                throw new DomModException("First digit of a CLZ must not be '0', node: " + nodeName);
            }
        }

        @Override
        public void validateParam(String param)
                throws DomModException {
            // intentionally empty
        }

    };

    public abstract void validateNodeContent(String nodeName, String nodeContent) throws DomModException;

    public abstract void validateParam(String param) throws DomModException;

    protected void checkLength(String prefix, int minLength, int maxLength, String content) {
        if (content == null || content.isEmpty()) {
            throw new DomModException(prefix + " has no text content");
        } else if (minLength >= 0 && content.length() < minLength) {
            throw new DomModException(prefix + " has less than " + minLength + " characters");
        } else if (maxLength >= 0 && content.length() > maxLength) {
            throw new DomModException(prefix + " has more than " + maxLength + " characters");
        }
    }

    protected void checkDigits(String prefix, String content) {
        for (char c : content.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new DomModException(prefix + " has invalid digits: " + content);
            }
        }
    }

    protected int checkNumeric(String prefix, String content) {
        int numericValue;
        try {
            numericValue = Integer.parseInt(content);
        } catch (NumberFormatException e) {
            throw new DomModException(prefix + " is not a numeric value: " + content, e);
        }
        return numericValue;
    }

}
